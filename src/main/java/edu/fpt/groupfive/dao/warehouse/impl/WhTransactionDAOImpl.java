package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.model.warehouse.InboundReceipt;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundReceiptResponseDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WhTransactionDAOImpl implements WhTransactionDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public ReceiptResult executeInboundTransaction(InboundReceipt receipt, List<AssetPlacementPlan> placements) {
        Map<Integer, List<Integer>> generatedAssetIdsMap = new HashMap<>();
        int receiptId = -1;

        String insertReceiptSql = "INSERT INTO wh_inbound_receipt (purchase_order_id, delivery_note, received_by, received_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertReceiptDetailSql = "INSERT INTO wh_inbound_receipt_detail (receipt_id, purchase_order_detail_id, asset_type_id, quantity_received) VALUES (?, ?, ?, ?)";
        
        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity + ?, asset_type_id = ? WHERE zone_id = ?";
        String insertAssetSql = "INSERT INTO asset (asset_name, asset_type_id, purchase_order_detail_id, receipt_detail_id, current_status, original_cost, acquisition_date) VALUES (?, ?, ?, ?, 'AVAILABLE', ?, SYSDATETIME())";
        String insertPlacementSql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, 'INBOUND', ?, SYSDATETIME(), ?)";
        
        String checkPoStatusSql = """
            SELECT pod.purchase_order_detail_id, pod.quantity, 
                   (SELECT COALESCE(SUM(quantity_received), 0) FROM wh_inbound_receipt_detail ird WHERE ird.purchase_order_detail_id = pod.purchase_order_detail_id) as total_received
            FROM purchase_order_details pod
            WHERE pod.purchase_order_id = ?
        """;
        String updatePoStatusSql = "UPDATE purchase_orders SET status = 'COMPLETED' WHERE purchase_order_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                 PreparedStatement psReceipt = connection.prepareStatement(insertReceiptSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psReceiptDetail = connection.prepareStatement(insertReceiptDetailSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdateZone = connection.prepareStatement(updateZoneSql);
                 PreparedStatement psAsset = connection.prepareStatement(insertAssetSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPlacement = connection.prepareStatement(insertPlacementSql);
                 PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psCheckPo = connection.prepareStatement(checkPoStatusSql);
                 PreparedStatement psUpdatePo = connection.prepareStatement(updatePoStatusSql)
            ) {
                // 1. Insert Inbound Receipt
                psReceipt.setInt(1, receipt.getPurchaseOrderId());
                psReceipt.setString(2, receipt.getDeliveryNote());
                psReceipt.setInt(3, receipt.getReceivedBy());
                psReceipt.setString(4, receipt.getNote());
                psReceipt.executeUpdate();

                try (ResultSet rsReceipt = psReceipt.getGeneratedKeys()) {
                    if (rsReceipt.next()) receiptId = rsReceipt.getInt(1);
                }

                // Group placements by poDetailId to create Receipt Details
                Map<Integer, Integer> detailQtyMap = new HashMap<>();
                Map<Integer, Integer> receiptDetailIdMap = new HashMap<>();
                for (AssetPlacementPlan plan : placements) {
                    detailQtyMap.put(plan.poDetailId(), detailQtyMap.getOrDefault(plan.poDetailId(), 0) + 1);
                }

                for (Map.Entry<Integer, Integer> entry : detailQtyMap.entrySet()) {
                    int poDetailId = entry.getKey();
                    int qty = entry.getValue();
                    int assetTypeId = placements.stream().filter(p -> p.poDetailId().equals(poDetailId)).findFirst().get().assetTypeId();

                    psReceiptDetail.setInt(1, receiptId);
                    psReceiptDetail.setInt(2, poDetailId);
                    psReceiptDetail.setInt(3, assetTypeId);
                    psReceiptDetail.setInt(4, qty);
                    psReceiptDetail.executeUpdate();

                    try (ResultSet rsRD = psReceiptDetail.getGeneratedKeys()) {
                        if (rsRD.next()) receiptDetailIdMap.put(poDetailId, rsRD.getInt(1));
                    }
                }

                // 2. Process each asset placement
                for (AssetPlacementPlan plan : placements) {
                    int assetTypeId = plan.assetTypeId();
                    int unitVolume = plan.unitVolume();
                    int targetZoneId = plan.targetZoneId();
                    String assetName = "Tài sản " + plan.assetTypeName();
                    int receiptDetailId = receiptDetailIdMap.get(plan.poDetailId());

                    // Update Zone Capacity
                    psUpdateZone.setInt(1, unitVolume);
                    psUpdateZone.setInt(2, assetTypeId);
                    psUpdateZone.setInt(3, targetZoneId);
                    psUpdateZone.executeUpdate();

                    // Create Asset
                    psAsset.setString(1, assetName);
                    psAsset.setInt(2, assetTypeId);
                    psAsset.setObject(3, plan.poDetailId());
                    psAsset.setInt(4, receiptDetailId);
                    psAsset.setBigDecimal(5, plan.price());
                    psAsset.executeUpdate();

                    int newAssetId = -1;
                    try (ResultSet rsAsset = psAsset.getGeneratedKeys()) {
                        if (rsAsset.next()) {
                            newAssetId = rsAsset.getInt(1);
                            generatedAssetIdsMap.computeIfAbsent(assetTypeId, k -> new ArrayList<>()).add(newAssetId);
                        }
                    }

                    // Insert Placement
                    psPlacement.setInt(1, newAssetId);
                    psPlacement.setInt(2, targetZoneId);
                    psPlacement.setInt(3, receipt.getReceivedBy());
                    psPlacement.setString(4, "Nhập kho (Phiếu #" + receiptId + ")");
                    psPlacement.executeUpdate();

                    // Insert Transaction
                    psTrans.setInt(1, newAssetId);
                    psTrans.setInt(2, targetZoneId);
                    psTrans.setInt(3, receipt.getReceivedBy());
                    psTrans.setString(4, "Nhập kho mua mới");
                    psTrans.executeUpdate();
                }

                // 3. Check PO Completion
                psCheckPo.setInt(1, receipt.getPurchaseOrderId());
                boolean allCompleted = true;
                try (ResultSet rsCheck = psCheckPo.executeQuery()) {
                    while (rsCheck.next()) {
                        if (rsCheck.getInt("total_received") < rsCheck.getInt("quantity")) {
                            allCompleted = false;
                            break;
                        }
                    }
                }

                if (allCompleted) {
                    psUpdatePo.setInt(1, receipt.getPurchaseOrderId());
                    psUpdatePo.executeUpdate();
                }

            }

            connection.commit();
            return new ReceiptResult(receiptId, generatedAssetIdsMap);

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                }
            }
            throw new RuntimeException("Lỗi khi ghi nhận nhập kho: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public Integer getReceivedQuantity(Integer poDetailId) {
        String sql = "SELECT COALESCE(SUM(quantity_received), 0) FROM wh_inbound_receipt_detail WHERE purchase_order_detail_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poDetailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy số lượng đã nhập: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public InboundSummaryResponseDTO getInboundReceiptSummary(Integer receiptId) {
        String receiptSql = """
            SELECT r.receipt_id, r.purchase_order_id, r.received_at, s.supplier_name
            FROM wh_inbound_receipt r
            JOIN purchase_orders po ON r.purchase_order_id = po.purchase_order_id
            JOIN supplier s ON po.supplier_id = s.supplier_id
            WHERE r.receipt_id = ?
        """;
        
        String detailsSql = """
            SELECT rd.asset_type_id, at.type_name, rd.quantity_received, a.asset_id
            FROM wh_inbound_receipt_detail rd
            JOIN asset_type at ON rd.asset_type_id = at.asset_type_id
            JOIN asset a ON rd.receipt_detail_id = a.receipt_detail_id
            WHERE rd.receipt_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection()) {
            InboundSummaryResponseDTO.InboundSummaryResponseDTOBuilder builder = InboundSummaryResponseDTO.builder();
            
            try (PreparedStatement psR = conn.prepareStatement(receiptSql)) {
                psR.setInt(1, receiptId);
                try (ResultSet rsR = psR.executeQuery()) {
                    if (rsR.next()) {
                        builder.receiptId(rsR.getInt("receipt_id"))
                               .purchaseOrderId(rsR.getInt("purchase_order_id"))
                               .supplierName(rsR.getString("supplier_name"))
                               .inboundDate(rsR.getTimestamp("received_at").toLocalDateTime());
                    } else {
                        return null;
                    }
                }
            }

            Map<Integer, InboundSummaryResponseDTO.AssetGroupDTO> groupMap = new HashMap<>();
            try (PreparedStatement psD = conn.prepareStatement(detailsSql)) {
                psD.setInt(1, receiptId);
                try (ResultSet rsD = psD.executeQuery()) {
                    while (rsD.next()) {
                        int typeId = rsD.getInt("asset_type_id");
                        int assetId = rsD.getInt("asset_id");
                        
                        groupMap.computeIfAbsent(typeId, k -> {
                            try {
                                return InboundSummaryResponseDTO.AssetGroupDTO.builder()
                                        .assetTypeName(rsD.getString("type_name"))
                                        .quantity(0)
                                        .assetIds(new ArrayList<>())
                                        .build();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        
                        InboundSummaryResponseDTO.AssetGroupDTO group = groupMap.get(typeId);
                        group.getAssetIds().add(assetId);
                    }
                }
            }
            
            for (InboundSummaryResponseDTO.AssetGroupDTO g : groupMap.values()) {
                g.setQuantity(g.getAssetIds().size());
            }

            builder.assetGroups(new ArrayList<>(groupMap.values()));
            return builder.build();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy thông tin phiếu nhập: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InboundReceipt> getReceiptsByPoId(Integer poId) {
        List<InboundReceipt> result = new ArrayList<>();
        String sql = "SELECT receipt_id, purchase_order_id, delivery_note, received_by, received_at, note FROM wh_inbound_receipt WHERE purchase_order_id = ? ORDER BY received_at DESC";
        
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(InboundReceipt.builder()
                            .receiptId(rs.getInt("receipt_id"))
                            .purchaseOrderId(rs.getInt("purchase_order_id"))
                            .deliveryNote(rs.getString("delivery_note"))
                            .receivedBy(rs.getInt("received_by"))
                            .receivedAt(rs.getTimestamp("received_at").toLocalDateTime())
                            .note(rs.getString("note"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách phiếu nhập: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<InboundReceiptResponseDTO> getAllInboundReceipts() {
        List<InboundReceiptResponseDTO> result = new ArrayList<>();
        String sql = """
            SELECT r.receipt_id, r.purchase_order_id, r.delivery_note, r.received_at, 
                   s.supplier_name, CONCAT(u.first_name, ' ', u.last_name) as receiver_name,
                   (SELECT SUM(quantity_received) FROM wh_inbound_receipt_detail rd WHERE rd.receipt_id = r.receipt_id) as total_assets
            FROM wh_inbound_receipt r
            JOIN purchase_orders po ON r.purchase_order_id = po.purchase_order_id
            JOIN supplier s ON po.supplier_id = s.supplier_id
            JOIN users u ON r.received_by = u.user_id
            ORDER BY r.received_at DESC
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(InboundReceiptResponseDTO.builder()
                        .receiptId(rs.getInt("receipt_id"))
                        .purchaseOrderId(rs.getInt("purchase_order_id"))
                        .supplierName(rs.getString("supplier_name"))
                        .deliveryNote(rs.getString("delivery_note"))
                        .receivedBy(rs.getString("receiver_name"))
                        .receivedAt(rs.getTimestamp("received_at").toLocalDateTime())
                        .totalAssets(rs.getInt("total_assets"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy toàn bộ phiếu nhập: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void executeOutboundTransaction(Integer handoverId, Integer assetId, Integer zoneId, Integer executedBy, String note) {
        String deletePlacementSql = "DELETE FROM wh_asset_placement WHERE asset_id = ?";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, 'OUTBOUND', ?, SYSDATETIME(), ?)";
        String insertMapSql = "INSERT INTO map_handover_transactions (asset_handover_id, transaction_id) VALUES (?, ?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                PreparedStatement psDelete = connection.prepareStatement(deletePlacementSql);
                PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psMap = connection.prepareStatement(insertMapSql)
            ) {
                // 1. Delete placement
                psDelete.setInt(1, assetId);
                psDelete.executeUpdate();

                // 2. Insert transaction
                psTrans.setInt(1, assetId);
                psTrans.setInt(2, zoneId);
                psTrans.setInt(3, executedBy);
                psTrans.setString(4, note);
                psTrans.executeUpdate();

                int transId = -1;
                try (ResultSet rs = psTrans.getGeneratedKeys()) {
                    if (rs.next()) {
                        transId = rs.getInt(1);
                    }
                }

                // 3. Map handover transaction
                if (transId != -1) {
                    psMap.setInt(1, handoverId);
                    psMap.setInt(2, transId);
                    psMap.executeUpdate();
                }
            }

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                }
            }
            throw new RuntimeException("Lỗi khi thực hiện xuất kho: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                t.transaction_id, 
                t.transaction_type, 
                a.asset_name, 
                z.zone_name, 
                CONCAT(u.first_name, ' ', u.last_name) AS executed_by, 
                t.executed_at,
                COALESCE(po.purchase_order_id, ho.asset_handover_id) AS reference_id,
                CASE 
                    WHEN po.purchase_order_id IS NOT NULL THEN 'PO'
                    WHEN ho.asset_handover_id IS NOT NULL THEN 'HANDOVER'
                    ELSE 'OTHER' 
                END AS reference_type
            FROM wh_transactions t
            JOIN asset a ON t.asset_id = a.asset_id
            JOIN wh_zones z ON t.zone_id = z.zone_id
            JOIN users u ON t.executed_by = u.user_id
            LEFT JOIN map_po_transactions po ON t.transaction_id = po.transaction_id
            LEFT JOIN map_handover_transactions ho ON t.transaction_id = ho.transaction_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (filter != null) {
            if (filter.getFromDate() != null) {
                sql.append(" AND CAST(t.executed_at AS DATE) >= ?");
                params.add(java.sql.Date.valueOf(filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                sql.append(" AND CAST(t.executed_at AS DATE) <= ?");
                params.add(java.sql.Date.valueOf(filter.getToDate()));
            }
            if (filter.getTransactionType() != null && !filter.getTransactionType().isBlank()) {
                sql.append(" AND t.transaction_type = ?");
                params.add(filter.getTransactionType());
            }
        }

        sql.append(" ORDER BY t.executed_at DESC");

        List<LedgerRecordResponseDTO> result = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
             
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LedgerRecordResponseDTO dto = LedgerRecordResponseDTO.builder()
                            .transactionId(rs.getInt("transaction_id"))
                            .transactionType(rs.getString("transaction_type"))
                            .assetName(rs.getString("asset_name"))
                            .zoneName(rs.getString("zone_name"))
                            .executedBy(rs.getString("executed_by"))
                            .executedAt(rs.getTimestamp("executed_at") != null 
                                    ? rs.getTimestamp("executed_at").toLocalDateTime() 
                                    : null)
                            .referenceId(rs.getObject("reference_id") != null 
                                     ? rs.getInt("reference_id") 
                                     : null)
                            .referenceType(rs.getString("reference_type"))
                            .build();
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải danh sách giao dịch kho: " + e.getMessage(), e);
        }

        return result;
    }
}
