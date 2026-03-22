package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;

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
    public Map<Integer, List<Integer>> processInboundPO(Integer poId, Integer executedBy, List<InboundAssetData> assetsToInbound) {
        Map<Integer, List<Integer>> generatedAssetIdsMap = new HashMap<>();

        String getVolumeSql = "SELECT unit_volume FROM wh_asset_capacity WHERE asset_type_id = ?";
        String fillUpSql = "SELECT TOP 1 zone_id FROM wh_zones WHERE status = 'ACTIVE' AND asset_type_id = ? AND (max_capacity - current_capacity) >= ? ORDER BY current_capacity DESC";
        String newZoneSql = "SELECT TOP 1 zone_id FROM wh_zones WHERE status = 'ACTIVE' AND asset_type_id IS NULL AND max_capacity >= ?";
        String updateZoneSql = "UPDATE wh_zones SET current_capacity = current_capacity + ?, asset_type_id = ? WHERE zone_id = ?";

        String insertAssetSql = "INSERT INTO asset (asset_name, asset_type_id, purchase_order_detail_id, current_status, original_cost, acquisition_date) VALUES (?, ?, ?, 'AVAILABLE', ?, SYSDATETIME())";
        String insertPlacementSql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, SYSDATETIME(), ?)";
        String insertTransactionSql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, 'INBOUND', ?, SYSDATETIME(), ?)";
        String insertMapSql = "INSERT INTO map_po_transactions (purchase_order_id, transaction_id) VALUES (?, ?)";
        String updatePoStatusSql = "UPDATE purchase_orders SET status = 'COMPLETED' WHERE purchase_order_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (
                 PreparedStatement psVolume = connection.prepareStatement(getVolumeSql);
                 PreparedStatement psFillUp = connection.prepareStatement(fillUpSql);
                 PreparedStatement psNewZone = connection.prepareStatement(newZoneSql);
                 PreparedStatement psUpdateZone = connection.prepareStatement(updateZoneSql);
                 PreparedStatement psAsset = connection.prepareStatement(insertAssetSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPlacement = connection.prepareStatement(insertPlacementSql);
                 PreparedStatement psTrans = connection.prepareStatement(insertTransactionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psMap = connection.prepareStatement(insertMapSql);
                 PreparedStatement psUpdatePo = connection.prepareStatement(updatePoStatusSql)
            ) {
                for (InboundAssetData data : assetsToInbound) {
                    if (data.quantity() == null || data.quantity() <= 0) continue;

                    // 1. Get unit_volume
                    int unitVolume = 1; // Default
                    psVolume.setInt(1, data.assetTypeId());
                    try (ResultSet rsVol = psVolume.executeQuery()) {
                        if (rsVol.next()) {
                            unitVolume = rsVol.getInt("unit_volume");
                        }
                    }

                    List<Integer> generatedIds = new ArrayList<>();
                    String assetName = "Tài sản " + data.assetTypeName();

                    for (int i = 0; i < data.quantity(); i++) {
                        int targetZoneId = -1;

                        // 2. Fill-up Strategy
                        psFillUp.setInt(1, data.assetTypeId());
                        psFillUp.setInt(2, unitVolume);
                        try (ResultSet rsFill = psFillUp.executeQuery()) {
                            if (rsFill.next()) {
                                targetZoneId = rsFill.getInt("zone_id");
                            }
                        }

                        // 3. New Zone Strategy
                        if (targetZoneId == -1) {
                            psNewZone.setInt(1, unitVolume);
                            try (ResultSet rsNew = psNewZone.executeQuery()) {
                                if (rsNew.next()) {
                                    targetZoneId = rsNew.getInt("zone_id");
                                }
                            }
                        }

                        // 4. Exception if no zone found
                        if (targetZoneId == -1) {
                            throw new RuntimeException("Kho đã đầy, không tìm thấy Zone phù hợp để xếp tài sản: " + data.assetTypeName());
                        }

                        // Update Zone Capacity
                        psUpdateZone.setInt(1, unitVolume);
                        psUpdateZone.setInt(2, data.assetTypeId());
                        psUpdateZone.setInt(3, targetZoneId);
                        psUpdateZone.executeUpdate();

                        // Create Asset
                        psAsset.setString(1, assetName);
                        psAsset.setInt(2, data.assetTypeId());
                        psAsset.setObject(3, data.poDetailId());
                        psAsset.setBigDecimal(4, data.price());
                        psAsset.executeUpdate();

                        int newAssetId = -1;
                        try (ResultSet rsAsset = psAsset.getGeneratedKeys()) {
                            if (rsAsset.next()) {
                                newAssetId = rsAsset.getInt(1);
                                generatedIds.add(newAssetId);
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
                    }

                    generatedAssetIdsMap.put(data.assetTypeId(), generatedIds);
                }

                // Update PO Status
                psUpdatePo.setInt(1, poId);
                psUpdatePo.executeUpdate();

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
            throw new RuntimeException("Lỗi khi phân bổ và nhập kho: " + e.getMessage(), e);
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
    public List<LedgerRecordResponseDTO> getAllTransactions() {
        String sql = """
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
            ORDER BY t.executed_at DESC
        """;

        List<LedgerRecordResponseDTO> result = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải danh sách giao dịch kho: " + e.getMessage(), e);
        }

        return result;
    }
}
