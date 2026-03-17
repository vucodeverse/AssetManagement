package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseZoneDAO;
import edu.fpt.groupfive.dto.warehouse.request.ZoneRequest;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseDashboardResponse;
import edu.fpt.groupfive.dto.warehouse.response.TransactionHistoryResponse;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponse;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseZoneDAO warehouseZoneDAO;
    private final WarehouseMapper warehouseMapper;
    private final JdbcTemplate jdbcTemplate;
    private final AssetTypeService assetTypeService;

    @Override
    public WarehouseDashboardResponse getDashboardStats() {
        WarehouseDashboardResponse response = new WarehouseDashboardResponse();
        
        // 1. Pending PO (Đã duyệt nhưng chưa có trong wh_transactions hoặc map_po_transactions)
        // Simplified: Count APPROVED POs that don't have a transaction yet
        String sqlPO = "SELECT COUNT(*) FROM purchase_orders po " +
                       "WHERE po.status = 'APPROVED' " +
                       "AND NOT EXISTS (SELECT 1 FROM map_po_transactions mpt WHERE mpt.purchase_order_id = po.purchase_order_id)";
        response.setPendingPO(jdbcTemplate.queryForObject(sqlPO, Integer.class));

        // 2. Pending Return (Yêu cầu đã confirm nhưng chưa có trong wh_transactions)
        String sqlReturn = "SELECT COUNT(*) FROM return_request rr " +
                           "WHERE rr.status = 'CONFIRMED' " +
                           "AND NOT EXISTS (SELECT 1 FROM map_return_transactions mrt WHERE mrt.return_request_id = rr.request_id)";
        response.setPendingReturn(jdbcTemplate.queryForObject(sqlReturn, Integer.class));

        // 3. Pending Allocation (Đã duyệt cấp phát nhưng chưa xuất kho)
        String sqlAllocation = "SELECT COUNT(*) FROM allocation_request ar " +
                               "WHERE ar.status = 'APPROVED' " +
                               "AND NOT EXISTS (SELECT 1 FROM map_allocation_transactions mat WHERE mat.allocation_request_id = ar.request_id)";
        response.setPendingAllocation(jdbcTemplate.queryForObject(sqlAllocation, Integer.class));

        return response;
    }

    @Override
    public List<ZoneResponse> getAllZones() {
        var typeMap = assetTypeService.getAssetTypeIdToNameMap();
        // For simplicity, assuming one warehouse for now
        return warehouseZoneDAO.findByWarehouseId(1).stream()
                .map(zone -> {
                    ZoneResponse response = warehouseMapper.toResponse(zone);
                    if (zone.getAssetTypeId() != null) {
                        response.setAssetTypeName(typeMap.get(zone.getAssetTypeId()));
                    }
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public void createZone(ZoneRequest request) {
        WarehouseZone zone = warehouseMapper.toEntity(request);
        warehouseZoneDAO.insert(zone);
    }

    @Override
    @Transactional
    public void deleteZone(Integer zoneId) {
        warehouseZoneDAO.findById(zoneId).ifPresent(zone -> {
            zone.setStatus("INACTIVE");
            warehouseZoneDAO.update(zone);
        });
    }

    @Override
    @Transactional
    public void resetZone(Integer zoneId) {
        warehouseZoneDAO.findById(zoneId).ifPresent(zone -> {
            if (zone.getCurrentCapacity() == 0) {
                zone.setAssetTypeId(null);
                warehouseZoneDAO.update(zone);
            } else {
                throw new RuntimeException("Cannot reset zone with assets still placed.");
            }
        });
    }

    @Override
    public List<TransactionHistoryResponse> getTransactionHistory() {
        String sql = "SELECT t.transaction_id, t.asset_id, a.asset_name, t.zone_id, z.zone_name, " +
                     "t.transaction_type, u.first_name + ' ' + u.last_name as executed_by_name, " +
                     "t.executed_at, t.note " +
                     "FROM wh_transactions t " +
                     "JOIN asset a ON t.asset_id = a.asset_id " +
                     "JOIN wh_zones z ON t.zone_id = z.zone_id " +
                     "JOIN users u ON t.executed_by = u.user_id " +
                     "ORDER BY t.executed_at DESC";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TransactionHistoryResponse history = new TransactionHistoryResponse();
            history.setTransactionId(rs.getInt("transaction_id"));
            history.setAssetId(rs.getInt("asset_id"));
            history.setAssetName(rs.getString("asset_name"));
            history.setZoneId(rs.getInt("zone_id"));
            history.setZoneName(rs.getString("zone_name"));
            history.setTransactionType(rs.getString("transaction_type"));
            history.setExecutedByName(rs.getString("executed_by_name"));
            history.setExecutedAt(rs.getTimestamp("executed_at").toLocalDateTime());
            history.setNote(rs.getString("note"));
            return history;
        });
    }
}
