package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.warehouse.AssetCapacityDAO;
import edu.fpt.groupfive.dao.warehouse.AssetPlacementDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseTransactionDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseZoneDAO;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import edu.fpt.groupfive.model.warehouse.AssetPlacement;
import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import edu.fpt.groupfive.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WarehouseOutboundServiceImpl implements WarehouseOutboundService {

    private final AssetDAO assetDAO;
    private final WarehouseZoneDAO warehouseZoneDAO;
    private final AssetCapacityDAO assetCapacityDAO;
    private final AssetPlacementDAO assetPlacementDAO;
    private final WarehouseTransactionDAO warehouseTransactionDAO;
    private final SecurityUtils securityUtils;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void processAllocationOutbound(Integer allocationRequestId, Integer assetId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) throw new RuntimeException("Unauthorized");

        // 1. Validate Asset
        Asset asset = assetDAO.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        
        if (AssetStatus.AVAILABLE != asset.getCurrentStatus()) {
             throw new RuntimeException("Asset is not available in warehouse.");
        }

        AssetPlacement placement = assetPlacementDAO.findByAssetId(assetId)
                .orElseThrow(() -> new RuntimeException("Asset is not assigned to any warehouse zone."));

        WarehouseZone zone = warehouseZoneDAO.findById(placement.getZoneId())
                .orElseThrow(() -> new RuntimeException("Warehouse zone not found."));

        AssetCapacity capacity = assetCapacityDAO.findByAssetTypeId(asset.getAssetTypeId())
                .orElse(new AssetCapacity(asset.getAssetTypeId(), 1));

        // 2. Process Outbound
        // Update Asset Status
        asset.setCurrentStatus(AssetStatus.ASSIGNED);
        assetDAO.update(asset);

        // Record transaction
        WarehouseTransaction tx = new WarehouseTransaction();
        tx.setAssetId(assetId);
        tx.setZoneId(zone.getZoneId());
        tx.setTransactionType("OUTBOUND");
        tx.setExecutedBy(currentUserId);
        tx.setExecutedAt(LocalDateTime.now());
        tx.setNote("Outbound for Allocation Request " + allocationRequestId);
        int txId = warehouseTransactionDAO.insert(tx);

        // Map Allocation
        warehouseTransactionDAO.mapAllocationTransaction(allocationRequestId, txId);

        // 3. Update Warehouse state
        assetPlacementDAO.delete(assetId);

        zone.setCurrentCapacity(zone.getCurrentCapacity() - capacity.getUnitVolume());
        warehouseZoneDAO.update(zone);
    }

    @Override
    public List<Map<String, Object>> getPendingAllocations() {
        String sql = """
            SELECT ar.request_id, ar.request_date, u.first_name + ' ' + u.last_name as requester_name,
                   ad.asset_type_id, at.type_name, 
                   (ad.quantity_requested - (
                       SELECT COUNT(*) FROM map_allocation_transactions mat 
                       JOIN wh_transactions wt ON mat.transaction_id = wt.transaction_id
                       JOIN asset a ON wt.asset_id = a.asset_id
                       WHERE mat.allocation_request_id = ar.request_id AND a.asset_type_id = ad.asset_type_id
                   )) as quantity
            FROM allocation_request ar
            JOIN users u ON ar.requester_id = u.user_id
            JOIN allocation_request_detail ad ON ar.request_id = ad.request_id
            JOIN asset_type at ON ad.asset_type_id = at.asset_type_id
            WHERE ar.status = 'APPROVED'
            AND ad.quantity_requested > (
                SELECT COUNT(*) FROM map_allocation_transactions mat 
                JOIN wh_transactions wt ON mat.transaction_id = wt.transaction_id
                JOIN asset a ON wt.asset_id = a.asset_id
                WHERE mat.allocation_request_id = ar.request_id AND a.asset_type_id = ad.asset_type_id
            )
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
