package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.warehouse.AssetCapacityDAO;
import edu.fpt.groupfive.dao.warehouse.AssetPlacementDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseTransactionDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseZoneDAO;
import edu.fpt.groupfive.dto.warehouse.request.InboundPORequest;
import edu.fpt.groupfive.dto.warehouse.request.InboundReturnRequest;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import edu.fpt.groupfive.model.warehouse.AssetPlacement;
import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
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
public class WarehouseInboundServiceImpl implements WarehouseInboundService {

    private final AssetDAO assetDAO;
    private final WarehouseZoneDAO warehouseZoneDAO;
    private final AssetCapacityDAO assetCapacityDAO;
    private final AssetPlacementDAO assetPlacementDAO;
    private final WarehouseTransactionDAO warehouseTransactionDAO;
    private final SecurityUtils securityUtils;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void processPOInbound(InboundPORequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) throw new RuntimeException("Unauthorized");

        // 1. Validate PO status and remaining quantity
        String checkSql = """
            SELECT po.status, pd.asset_type_id as asset_type_id,
                   (pd.quantity - (
                       SELECT COUNT(*) FROM map_po_transactions mpt 
                       JOIN wh_transactions wt ON mpt.transaction_id = wt.transaction_id
                       JOIN asset a ON wt.asset_id = a.asset_id
                       WHERE mpt.purchase_order_id = po.purchase_order_id AND a.asset_type_id = pd.asset_type_id
                   )) as remaining
            FROM purchase_orders po
            JOIN purchase_order_details pd ON po.purchase_order_id = pd.purchase_order_id
            WHERE pd.purchase_order_detail_id = ?
        """;
        
        Map<String, Object> poInfo = jdbcTemplate.queryForMap(checkSql, request.getPurchaseOrderDetailId());
        String status = (String) poInfo.get("status");
        int remaining = ((Number) poInfo.get("remaining")).intValue();
        int actualAssetTypeId = ((Number) poInfo.get("asset_type_id")).intValue();

        if (!"PENDING".equals(status)) {
            throw new RuntimeException("Purchase Order is not in PENDING status.");
        }
        if (request.getQuantity() > remaining) {
            throw new RuntimeException("Inbound quantity exceeds remaining PO quantity (" + remaining + ").");
        }

        // 2. Get asset type volume for capacity check asset type
        AssetCapacity capacity = assetCapacityDAO.findByAssetTypeId(actualAssetTypeId)
                .orElse(new AssetCapacity(actualAssetTypeId, 1));

        int totalRequiredVolume = request.getQuantity() * capacity.getUnitVolume();

        // 3. Find available zones (Auto-Slotting)
        List<WarehouseZone> availableZones = warehouseZoneDAO.findAvailableZones(actualAssetTypeId, totalRequiredVolume);
        if (availableZones.isEmpty()) {
            throw new RuntimeException("No suitable zone found with sufficient capacity.");
        }

        WarehouseZone targetZone = availableZones.get(0);

        // 3. Create assets and place them
        for (int i = 0; i < request.getQuantity(); i++) {
            Asset asset = new Asset();
            String prefix = (request.getAssetNamePrefix() != null && !request.getAssetNamePrefix().trim().isEmpty()) 
                            ? request.getAssetNamePrefix().trim() + " " 
                            : ("Asset from PO " + request.getPurchaseOrderId() + " - Item ");
            asset.setAssetName(prefix + (i + 1));
            asset.setAssetTypeId(actualAssetTypeId);
            asset.setPurchaseOrderDetailId(request.getPurchaseOrderDetailId());
            asset.setCurrentStatus(AssetStatus.AVAILABLE);
            asset.setAcquisitionDate(java.time.LocalDate.now());
            
            // Insert and get generated ID
            int assetId = assetDAO.insert(asset);
            
            // Record placement
            AssetPlacement placement = new AssetPlacement();
            placement.setAssetId(assetId);
            placement.setZoneId(targetZone.getZoneId());
            placement.setPlacedBy(currentUserId);
            placement.setPlacedAt(LocalDateTime.now());
            assetPlacementDAO.insert(placement);

            // Record transaction
            WarehouseTransaction tx = new WarehouseTransaction();
            tx.setAssetId(assetId);
            tx.setZoneId(targetZone.getZoneId());
            tx.setTransactionType("INBOUND");
            tx.setExecutedBy(currentUserId);
            tx.setExecutedAt(LocalDateTime.now());
            tx.setNote(request.getNote());
            int txId = warehouseTransactionDAO.insert(tx);

            // Map PO
            warehouseTransactionDAO.mapPoTransaction(request.getPurchaseOrderId(), txId);
        }

        // 4. Update Zone capacity
        targetZone.setCurrentCapacity(targetZone.getCurrentCapacity() + totalRequiredVolume);
        if (targetZone.getAssetTypeId() == null) {
            targetZone.setAssetTypeId(actualAssetTypeId);
        }
        warehouseZoneDAO.update(targetZone);
    }

    @Override
    @Transactional
    public void processReturnInbound(InboundReturnRequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) throw new RuntimeException("Unauthorized");

        Asset asset = assetDAO.findById(request.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        AssetCapacity capacity = assetCapacityDAO.findByAssetTypeId(asset.getAssetTypeId())
                .orElse(new AssetCapacity(asset.getAssetTypeId(), 1));

        // Auto-Slotting
        List<WarehouseZone> availableZones = warehouseZoneDAO.findAvailableZones(asset.getAssetTypeId(), capacity.getUnitVolume());
        if (availableZones.isEmpty()) {
            throw new RuntimeException("No suitable zone found for return.");
        }

        WarehouseZone targetZone = availableZones.get(0);

        // Update Asset status
        asset.setCurrentStatus(AssetStatus.valueOf(request.getConditionStatus()));
        assetDAO.update(asset);

        // Record placement
        AssetPlacement placement = new AssetPlacement();
        placement.setAssetId(asset.getAssetId());
        placement.setZoneId(targetZone.getZoneId());
        placement.setPlacedBy(currentUserId);
        placement.setPlacedAt(LocalDateTime.now());
        assetPlacementDAO.insert(placement);

        // Record transaction
        WarehouseTransaction tx = new WarehouseTransaction();
        tx.setAssetId(asset.getAssetId());
        tx.setZoneId(targetZone.getZoneId());
        tx.setTransactionType("INBOUND");
        tx.setExecutedBy(currentUserId);
        tx.setExecutedAt(LocalDateTime.now());
        tx.setNote(request.getNote());
        int txId = warehouseTransactionDAO.insert(tx);

        // Map Return
        warehouseTransactionDAO.mapReturnTransaction(request.getReturnRequestId(), txId);

        // Update Zone
        targetZone.setCurrentCapacity(targetZone.getCurrentCapacity() + capacity.getUnitVolume());
        targetZone.setAssetTypeId(asset.getAssetTypeId());
        warehouseZoneDAO.update(targetZone);
    }

    @Override
    public List<Map<String, Object>> getPendingPOs() {
        String sql = """
            SELECT po.purchase_order_id as purchase_order_id, 
                   pd.purchase_order_detail_id as purchase_order_detail_id,
                   po.created_at as order_date, 
                   s.supplier_name as supplier_name,
                   pd.asset_type_id as asset_type_id, 
                   at.type_name as type_name, 
                   (pd.quantity - (
                       SELECT COUNT(*) FROM map_po_transactions mpt 
                       JOIN wh_transactions wt ON mpt.transaction_id = wt.transaction_id
                       JOIN asset a ON wt.asset_id = a.asset_id
                       WHERE mpt.purchase_order_id = po.purchase_order_id AND a.asset_type_id = pd.asset_type_id
                   )) as quantity
            FROM purchase_orders po
            JOIN supplier s ON po.supplier_id = s.supplier_id
            JOIN purchase_order_details pd ON po.purchase_order_id = pd.purchase_order_id
            JOIN asset_type at ON pd.asset_type_id = at.asset_type_id
            WHERE po.status = 'PENDING'
            AND pd.quantity > (
                 SELECT COUNT(*) FROM map_po_transactions mpt 
                 JOIN wh_transactions wt ON mpt.transaction_id = wt.transaction_id
                 JOIN asset a ON wt.asset_id = a.asset_id
                 WHERE mpt.purchase_order_id = po.purchase_order_id AND a.asset_type_id = pd.asset_type_id
            )
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getPendingReturns() {
        String sql = """
            SELECT rr.request_id as request_id, 
                   rr.request_date as request_date, 
                   u.first_name + ' ' + u.last_name as user_name,
                   rrd.asset_id as asset_id, 
                   a.asset_name as asset_name, 
                   at.type_name as type_name
            FROM return_request rr
            JOIN users u ON rr.requester_id = u.user_id
            JOIN return_request_detail rrd ON rr.request_id = rrd.request_id
            JOIN asset a ON rrd.asset_id = a.asset_id
            JOIN asset_type at ON a.asset_type_id = at.asset_type_id
            WHERE rr.status = 'APPROVED'
            AND NOT EXISTS (
                SELECT 1 FROM map_return_transactions mrt 
                JOIN wh_transactions wt ON mrt.transaction_id = wt.transaction_id
                WHERE mrt.return_request_id = rr.request_id AND wt.asset_id = rrd.asset_id
            )
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
