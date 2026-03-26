package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.model.warehouse.InventoryVoucher;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.InventoryVoucherResponseDTO;

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
    public ReceiptResult executeInboundTransaction(InventoryVoucher voucher, List<AssetPlacementPlan> placements) {
        Map<Integer, List<Integer>> generatedAssetIdsMap = new HashMap<>();
        int voucherId = -1;

        String insertVoucherSql = "INSERT INTO wh_inventory_vouchers (voucher_code, voucher_type, purchase_order_id, created_by, created_at, status, note) VALUES (?, 'INBOUND', ?, ?, SYSDATETIME(), 'COMPLETED', ?)";
        String insertVoucherDetailSql = "INSERT INTO wh_inventory_voucher_details (voucher_id, asset_type_id, purchase_order_detail_id, quantity) VALUES (?, ?, ?, ?)";
        
        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity + ?, asset_type_id = ? WHERE zone_id = ?";
        String insertAssetSql = "INSERT INTO asset (asset_name, asset_type_id, purchase_order_detail_id, voucher_detail_id, current_status, original_cost, acquisition_date) VALUES (?, ?, ?, ?, 'AVAILABLE', ?, SYSDATETIME())";
        String insertPlacementSql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, 'INBOUND', ?, SYSDATETIME(), ?)";
        
        String checkPoStatusSql = """
            SELECT pod.purchase_order_detail_id, pod.quantity, 
                   (SELECT COALESCE(SUM(quantity), 0) FROM wh_inventory_voucher_details ivd 
                    JOIN wh_inventory_vouchers iv ON ivd.voucher_id = iv.voucher_id
                    WHERE ivd.purchase_order_detail_id = pod.purchase_order_detail_id AND iv.voucher_type = 'INBOUND') as total_received
            FROM purchase_order_details pod
            WHERE pod.purchase_order_id = ?
        """;
        String updatePoStatusSql = "UPDATE purchase_orders SET status = 'COMPLETED' WHERE purchase_order_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                 PreparedStatement psVoucher = connection.prepareStatement(insertVoucherSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psVoucherDetail = connection.prepareStatement(insertVoucherDetailSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdateZone = connection.prepareStatement(updateZoneSql);
                 PreparedStatement psAsset = connection.prepareStatement(insertAssetSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPlacement = connection.prepareStatement(insertPlacementSql);
                 PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql);
                 PreparedStatement psCheckPo = connection.prepareStatement(checkPoStatusSql);
                 PreparedStatement psUpdatePo = connection.prepareStatement(updatePoStatusSql)
            ) {
                // 1. Insert Voucher
                psVoucher.setString(1, voucher.getVoucherCode());
                psVoucher.setObject(2, voucher.getPurchaseOrderId());
                psVoucher.setInt(3, voucher.getCreatedBy());
                psVoucher.setString(4, voucher.getNote());
                psVoucher.executeUpdate();

                try (ResultSet rsVoucher = psVoucher.getGeneratedKeys()) {
                    if (rsVoucher.next()) voucherId = rsVoucher.getInt(1);
                }

                // Group placements by poDetailId to create Voucher Details
                Map<Integer, Integer> detailQtyMap = new HashMap<>();
                Map<Integer, Integer> voucherDetailIdMap = new HashMap<>();
                for (AssetPlacementPlan plan : placements) {
                    detailQtyMap.put(plan.poDetailId(), detailQtyMap.getOrDefault(plan.poDetailId(), 0) + 1);
                }

                for (Map.Entry<Integer, Integer> entry : detailQtyMap.entrySet()) {
                    int poDetailId = entry.getKey();
                    int qty = entry.getValue();
                    int assetTypeId = placements.stream().filter(p -> p.poDetailId().equals(poDetailId)).findFirst().get().assetTypeId();

                    psVoucherDetail.setInt(1, voucherId);
                    psVoucherDetail.setInt(2, assetTypeId);
                    psVoucherDetail.setInt(3, poDetailId);
                    psVoucherDetail.setInt(4, qty);
                    psVoucherDetail.executeUpdate();

                    try (ResultSet rsVD = psVoucherDetail.getGeneratedKeys()) {
                        if (rsVD.next()) voucherDetailIdMap.put(poDetailId, rsVD.getInt(1));
                    }
                }

                // 2. Process each asset placement
                for (AssetPlacementPlan plan : placements) {
                    int assetTypeId = plan.assetTypeId();
                    int unitVolume = plan.unitVolume();
                    int targetZoneId = plan.targetZoneId();
                    String assetName = "Tài sản " + plan.assetTypeName();
                    int voucherDetailId = voucherDetailIdMap.get(plan.poDetailId());

                    // Update Zone Capacity
                    psUpdateZone.setInt(1, unitVolume);
                    psUpdateZone.setInt(2, assetTypeId);
                    psUpdateZone.setInt(3, targetZoneId);
                    psUpdateZone.executeUpdate();

                    // Create Asset
                    psAsset.setString(1, assetName);
                    psAsset.setInt(2, assetTypeId);
                    psAsset.setObject(3, plan.poDetailId());
                    psAsset.setInt(4, voucherDetailId);
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
                    psPlacement.setInt(3, voucher.getCreatedBy());
                    psPlacement.setString(4, "Nhập kho (Phiếu #" + voucher.getVoucherCode() + ")");
                    psPlacement.executeUpdate();

                    // Insert Transaction
                    psTrans.setInt(1, newAssetId);
                    psTrans.setInt(2, targetZoneId);
                    psTrans.setString(3, "INBOUND");
                    psTrans.setInt(4, voucher.getCreatedBy());
                    psTrans.setString(5, "Nhập kho mua mới (Phiếu #" + voucher.getVoucherCode() + ")");
                    psTrans.executeUpdate();
                }

                // 3. Check PO Completion
                if (voucher.getPurchaseOrderId() != null) {
                    psCheckPo.setInt(1, voucher.getPurchaseOrderId());
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
                        psUpdatePo.setInt(1, voucher.getPurchaseOrderId());
                        psUpdatePo.executeUpdate();
                    }
                }
            }

            connection.commit();
            return new ReceiptResult(voucherId, generatedAssetIdsMap);

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
    public void executeOutboundForHandover(InventoryVoucher voucher, Map<Integer, Integer> assets) {
        String insertVoucherSql = "INSERT INTO wh_inventory_vouchers (voucher_code, voucher_type, handover_id, created_by, created_at, status, note) VALUES (?, 'OUTBOUND', ?, ?, SYSDATETIME(), 'COMPLETED', ?)";
        String insertVoucherDetailSql = "INSERT INTO wh_inventory_voucher_details (voucher_id, asset_id, asset_type_id, quantity) VALUES (?, ?, ?, 1)";
        
        String insertHandoverDetailSql = "INSERT INTO asset_handover_detail (handover_id, asset_id, condition_status, note) VALUES (?, ?, 'GOOD', ?)";
        String updateAssetSql = "UPDATE asset SET current_status = 'ASSIGNED', department_id = (SELECT to_department_id FROM asset_handover WHERE handover_id = ?) WHERE asset_id = ?";
        
        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity - (SELECT COALESCE(unit_volume, 1) FROM wh_asset_capacity WHERE asset_type_id = (SELECT asset_type_id FROM asset WHERE asset_id = ?)) WHERE zone_id = ?";
        String deletePlacementSql = "DELETE FROM wh_asset_placement WHERE asset_id = ?";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, 'OUTBOUND', ?, SYSDATETIME(), ?)";
        
        String updateHandoverStatusSql = "UPDATE asset_handover SET status = 'COMPLETED' WHERE handover_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            int voucherId = -1;
            try (
                PreparedStatement psVoucher = connection.prepareStatement(insertVoucherSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psVoucherDetail = connection.prepareStatement(insertVoucherDetailSql);
                PreparedStatement psHandoverDetail = connection.prepareStatement(insertHandoverDetailSql);
                PreparedStatement psUpdateAsset = connection.prepareStatement(updateAssetSql);
                PreparedStatement psUpdateZone = connection.prepareStatement(updateZoneSql);
                PreparedStatement psDeletePlacement = connection.prepareStatement(deletePlacementSql);
                PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql);
                PreparedStatement psUpdateHandover = connection.prepareStatement(updateHandoverStatusSql)
            ) {
                // 1. Insert Voucher
                psVoucher.setString(1, voucher.getVoucherCode());
                psVoucher.setObject(2, voucher.getHandoverId());
                psVoucher.setInt(3, voucher.getCreatedBy());
                psVoucher.setString(4, voucher.getNote());
                psVoucher.executeUpdate();

                try (ResultSet rsV = psVoucher.getGeneratedKeys()) {
                    if (rsV.next()) voucherId = rsV.getInt(1);
                }

                // 2. Process each asset
                for (Map.Entry<Integer, Integer> entry : assets.entrySet()) {
                    int assetId = entry.getKey();
                    int zoneId = entry.getValue();

                    // Get Asset Type ID for voucher detail
                    int assetTypeId = -1;
                    try (PreparedStatement psType = connection.prepareStatement("SELECT asset_type_id FROM asset WHERE asset_id = ?")) {
                        psType.setInt(1, assetId);
                        try (ResultSet rsType = psType.executeQuery()) {
                            if (rsType.next()) assetTypeId = rsType.getInt(1);
                        }
                    }

                    // Voucher Detail
                    psVoucherDetail.setInt(1, voucherId);
                    psVoucherDetail.setInt(2, assetId);
                    psVoucherDetail.setInt(3, assetTypeId);
                    psVoucherDetail.executeUpdate();

                    // Handover Detail (Gắn tài sản vào handover detail theo yêu cầu)
                    psHandoverDetail.setInt(1, voucher.getHandoverId());
                    psHandoverDetail.setInt(2, assetId);
                    psHandoverDetail.setString(3, "Xuất kho cấp phát (Phiếu #" + voucher.getVoucherCode() + ")");
                    psHandoverDetail.executeUpdate();

                    // Update Asset
                    psUpdateAsset.setInt(1, voucher.getHandoverId());
                    psUpdateAsset.setInt(2, assetId);
                    psUpdateAsset.executeUpdate();

                    // Update Zone Capacity
                    psUpdateZone.setInt(1, assetId);
                    psUpdateZone.setInt(2, zoneId);
                    psUpdateZone.executeUpdate();

                    // Delete Placement
                    psDeletePlacement.setInt(1, assetId);
                    psDeletePlacement.executeUpdate();

                    // Insert Transaction
                    psTrans.setInt(1, assetId);
                    psTrans.setInt(2, zoneId);
                    psTrans.setInt(3, voucher.getCreatedBy());
                    psTrans.setString(4, "Xuất kho cấp phát (Phiếu #" + voucher.getVoucherCode() + ")");
                    psTrans.executeUpdate();
                }

                // 3. Update Handover Status
                psUpdateHandover.setInt(1, voucher.getHandoverId());
                psUpdateHandover.executeUpdate();
            }

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                }
            }
            throw new RuntimeException("Lỗi khi thực hiện xuất kho cấp phát: " + e.getMessage(), e);
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
        String sql = """
            SELECT COALESCE(SUM(quantity), 0) 
            FROM wh_inventory_voucher_details ivd
            JOIN wh_inventory_vouchers iv ON ivd.voucher_id = iv.voucher_id
            WHERE ivd.purchase_order_detail_id = ? AND iv.voucher_type = 'INBOUND'
        """;
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
    public InboundSummaryResponseDTO getInboundReceiptSummary(Integer voucherId) {
        String voucherSql = """
            SELECT v.voucher_id, v.voucher_code, v.purchase_order_id, v.created_at, s.supplier_name
            FROM wh_inventory_vouchers v
            LEFT JOIN purchase_orders po ON v.purchase_order_id = po.purchase_order_id
            LEFT JOIN supplier s ON po.supplier_id = s.supplier_id
            WHERE v.voucher_id = ?
        """;
        
        String detailsSql = """
            SELECT vd.asset_type_id, at.type_name, a.asset_id
            FROM wh_inventory_voucher_details vd
            JOIN asset_type at ON vd.asset_type_id = at.asset_type_id
            JOIN asset a ON vd.voucher_detail_id = a.voucher_detail_id
            WHERE vd.voucher_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection()) {
            InboundSummaryResponseDTO.InboundSummaryResponseDTOBuilder builder = InboundSummaryResponseDTO.builder();
            
            try (PreparedStatement psV = conn.prepareStatement(voucherSql)) {
                psV.setInt(1, voucherId);
                try (ResultSet rsV = psV.executeQuery()) {
                    if (rsV.next()) {
                        builder.receiptId(rsV.getInt("voucher_id"))
                               .purchaseOrderId(rsV.getInt("purchase_order_id"))
                               .supplierName(rsV.getString("supplier_name"))
                               .inboundDate(rsV.getTimestamp("created_at").toLocalDateTime());
                    } else {
                        return null;
                    }
                }
            }

            Map<Integer, InboundSummaryResponseDTO.AssetGroupDTO> groupMap = new HashMap<>();
            try (PreparedStatement psD = conn.prepareStatement(detailsSql)) {
                psD.setInt(1, voucherId);
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
    public List<InventoryVoucher> getVouchersByPoId(Integer poId) {
        List<InventoryVoucher> result = new ArrayList<>();
        String sql = "SELECT voucher_id, voucher_code, voucher_type, purchase_order_id, created_by, created_at, status, note FROM wh_inventory_vouchers WHERE purchase_order_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(InventoryVoucher.builder()
                            .voucherId(rs.getInt("voucher_id"))
                            .voucherCode(rs.getString("voucher_code"))
                            .voucherType(rs.getString("voucher_type"))
                            .purchaseOrderId(rs.getInt("purchase_order_id"))
                            .createdBy(rs.getInt("created_by"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .status(rs.getString("status"))
                            .note(rs.getString("note"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách phiếu kho: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<InventoryVoucherResponseDTO> getAllInboundVouchers() {
        List<InventoryVoucherResponseDTO> result = new ArrayList<>();
        String sql = """
            SELECT v.voucher_id, v.voucher_code, v.purchase_order_id, v.created_at, 
                   s.supplier_name, CONCAT(u.first_name, ' ', u.last_name) as creator_name,
                   (SELECT SUM(quantity) FROM wh_inventory_voucher_details vd WHERE vd.voucher_id = v.voucher_id) as total_assets
            FROM wh_inventory_vouchers v
            LEFT JOIN purchase_orders po ON v.purchase_order_id = po.purchase_order_id
            LEFT JOIN supplier s ON po.supplier_id = s.supplier_id
            JOIN users u ON v.created_by = u.user_id
            WHERE v.voucher_type = 'INBOUND'
            ORDER BY v.created_at DESC
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(InventoryVoucherResponseDTO.builder()
                        .voucherId(rs.getInt("voucher_id"))
                        .purchaseOrderId(rs.getInt("purchase_order_id"))
                        .supplierName(rs.getString("supplier_name"))
                        .voucherCode(rs.getString("voucher_code"))
                        .createdBy(rs.getString("creator_name"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .totalAssets(rs.getInt("total_assets"))
                        .voucherType("INBOUND")
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy toàn bộ phiếu nhập: " + e.getMessage(), e);
        }
        return result;
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
                COALESCE(iv.purchase_order_id, iv.handover_id) AS reference_id,
                CASE 
                    WHEN iv.purchase_order_id IS NOT NULL THEN 'PO'
                    WHEN iv.handover_id IS NOT NULL THEN 'HANDOVER'
                    ELSE 'OTHER' 
                END AS reference_type
            FROM wh_transactions t
            JOIN asset a ON t.asset_id = a.asset_id
            JOIN wh_zones z ON t.zone_id = z.zone_id
            JOIN users u ON t.executed_by = u.user_id
            -- Link to voucher if needed (optional based on schema)
            -- Note: wh_transactions doesn't have voucher_id in schema yet, adding it in recreate_db.sql if needed
            -- but let's assume we use map tables for now or direct link if I added it.
            -- Actually, I will search for the reference via vouchers or use the old map logic if not updated.
            -- In my recreate_db update, I kept the map tables but they are not really needed with vouchers.
            -- For simplicity in this turn, I will assume the map tables still exist or link via asset -> voucher -> handover
            LEFT JOIN wh_inventory_voucher_details ivd ON a.voucher_detail_id = ivd.voucher_detail_id
            LEFT JOIN wh_inventory_vouchers iv ON ivd.voucher_id = iv.voucher_id
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
