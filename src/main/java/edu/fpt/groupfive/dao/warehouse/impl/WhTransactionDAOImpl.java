package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;

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
    public Map<Integer, List<Integer>> executeInboundTransaction(Integer poId, Integer executedBy, List<AssetPlacementPlan> placements, Integer receiptId) {
        Map<Integer, List<Integer>> generatedAssetIdsMap = new HashMap<>();

        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity + ?, asset_type_id = ? WHERE zone_id = ?";
        String insertAssetSql = "INSERT INTO asset (asset_name, asset_type_id, purchase_order_detail_id, current_status, original_cost, acquisition_date) VALUES (?, ?, ?, 'AVAILABLE', ?, SYSDATETIME())";
        String insertPlacementSql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note, receipt_id) VALUES (?, ?, 'INBOUND', ?, SYSDATETIME(), ?, ?)";
        String insertMapSql = "INSERT INTO map_po_transactions (purchase_order_id, transaction_id) VALUES (?, ?)";
        String updateReceivedQtySql = "UPDATE purchase_order_details SET received_quantity = received_quantity + 1 WHERE purchase_order_detail_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                 PreparedStatement psUpdateZone = connection.prepareStatement(updateZoneSql);
                 PreparedStatement psAsset = connection.prepareStatement(insertAssetSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPlacement = connection.prepareStatement(insertPlacementSql);
                 PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psMap = connection.prepareStatement(insertMapSql);
                 PreparedStatement psUpdateReceived = connection.prepareStatement(updateReceivedQtySql)
            ) {
                for (AssetPlacementPlan plan : placements) {
                    int assetTypeId = plan.assetTypeId();
                    int unitVolume = plan.unitVolume();
                    int targetZoneId = plan.targetZoneId();
                    String assetName = "Tài sản " + plan.assetTypeName();

                    // Update Zone Capacity
                    psUpdateZone.setInt(1, unitVolume);
                    psUpdateZone.setInt(2, assetTypeId);
                    psUpdateZone.setInt(3, targetZoneId);
                    psUpdateZone.executeUpdate();

                    // Create Asset
                    psAsset.setString(1, assetName);
                    psAsset.setInt(2, assetTypeId);
                    psAsset.setObject(3, plan.poDetailId());
                    psAsset.setBigDecimal(4, plan.price());
                    psAsset.executeUpdate();

                    int newAssetId = -1;
                    try (ResultSet rsAsset = psAsset.getGeneratedKeys()) {
                        if (rsAsset.next()) {
                            newAssetId = rsAsset.getInt(1);
                            generatedAssetIdsMap.computeIfAbsent(assetTypeId, k -> new ArrayList<>()).add(newAssetId);
                        } else {
                            throw new RuntimeException("Không lấy được ID tài sản sau khi insert.");
                        }
                    }

                    // Insert Placement
                    psPlacement.setInt(1, newAssetId);
                    psPlacement.setInt(2, targetZoneId);
                    psPlacement.setInt(3, executedBy);
                    psPlacement.setString(4, "Nhập kho từ PO #" + poId);
                    psPlacement.executeUpdate();

                    // Insert Transaction
                    psTrans.setInt(1, newAssetId);
                    psTrans.setInt(2, targetZoneId);
                    psTrans.setInt(3, executedBy);
                    psTrans.setString(4, "Nhập kho tự động (PO)");
                    psTrans.setObject(5, receiptId); // LINK TO RECEIPT
                    psTrans.executeUpdate();

                    int newTransId = -1;
                    try (ResultSet rsTrans = psTrans.getGeneratedKeys()) {
                        if (rsTrans.next()) {
                            newTransId = rsTrans.getInt(1);
                        } else {
                            throw new RuntimeException("Không lấy được ID Transaction sau khi insert.");
                        }
                    }

                    // Map PO Transaction
                    psMap.setInt(1, poId);
                    psMap.setInt(2, newTransId);
                    psMap.executeUpdate();

                    // Update Received Quantity in PO Detail
                    if (plan.poDetailId() != null) {
                        psUpdateReceived.setInt(1, plan.poDetailId());
                        psUpdateReceived.executeUpdate();
                    }
                }

            }

            connection.commit();
            return generatedAssetIdsMap;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Ignore rollback errors
                }
            }
            throw new RuntimeException("Lỗi khi ghi nhận dữ liệu vào DataBase: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ex) {
                    // Ignore close errors
                }
            }
        }
    }

    @Override
    public void executeReturnInboundTransaction(Integer handoverId, Integer assetId, Integer zoneId, Integer executedBy, String note, Integer receiptId) {
        String updateAssetSql = "UPDATE asset SET current_status = 'AVAILABLE', department_id = NULL WHERE asset_id = ?";
        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity + (SELECT unit_volume FROM wh_asset_capacity WHERE asset_type_id = (SELECT asset_type_id FROM asset WHERE asset_id = ?)) WHERE zone_id = ?";
        String insertPlacementSql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note, receipt_id) VALUES (?, ?, 'INBOUND', ?, SYSDATETIME(), ?, ?)";
        String insertMapSql = "INSERT INTO map_handover_transactions (asset_handover_id, transaction_id) VALUES (?, ?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                PreparedStatement psAsset = connection.prepareStatement(updateAssetSql);
                PreparedStatement psZone = connection.prepareStatement(updateZoneSql);
                PreparedStatement psPlacement = connection.prepareStatement(insertPlacementSql);
                PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psMap = connection.prepareStatement(insertMapSql)
            ) {
                // 1. Update Asset
                psAsset.setInt(1, assetId);
                psAsset.executeUpdate();

                // 2. Update Zone
                psZone.setInt(1, assetId);
                psZone.setInt(2, zoneId);
                psZone.executeUpdate();

                // 3. Insert Placement (if already exists, update instead)
                try (PreparedStatement psDelPlace = connection.prepareStatement("DELETE FROM wh_asset_placement WHERE asset_id = ?")) {
                    psDelPlace.setInt(1, assetId);
                    psDelPlace.executeUpdate();
                }
                psPlacement.setInt(1, assetId);
                psPlacement.setInt(2, zoneId);
                psPlacement.setInt(3, executedBy);
                psPlacement.setString(4, note);
                psPlacement.executeUpdate();

                // 4. Insert Transaction
                psTrans.setInt(1, assetId);
                psTrans.setInt(2, zoneId);
                psTrans.setInt(3, executedBy);
                psTrans.setString(4, "Thu hồi từ lệnh bàn giao #" + handoverId);
                psTrans.setObject(5, receiptId); // LINK TO RECEIPT
                psTrans.executeUpdate();

                int transId = -1;
                try (ResultSet rs = psTrans.getGeneratedKeys()) {
                    if (rs.next()) {
                        transId = rs.getInt(1);
                    }
                }

                // 5. Map handover transaction
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
            throw new RuntimeException("Lỗi khi thực hiện nhập kho thu hồi: " + e.getMessage(), e);
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
                params.add(Date.valueOf(filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                sql.append(" AND CAST(t.executed_at AS DATE) <= ?");
                params.add(Date.valueOf(filter.getToDate()));
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
