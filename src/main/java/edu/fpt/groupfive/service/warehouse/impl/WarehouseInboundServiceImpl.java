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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseInboundServiceImpl implements WarehouseInboundService {

    private final AssetDAO assetDAO;
    private final WarehouseZoneDAO warehouseZoneDAO;
    private final AssetCapacityDAO assetCapacityDAO;
    private final AssetPlacementDAO assetPlacementDAO;
    private final WarehouseTransactionDAO warehouseTransactionDAO;

    @Override
    @Transactional
    public void processPOInbound(InboundPORequest request) {
        // 1. Get unit volume for the asset type
        AssetCapacity capacity = assetCapacityDAO.findByAssetTypeId(request.getAssetTypeId())
                .orElse(new AssetCapacity(request.getAssetTypeId(), 1));

        int totalRequiredVolume = request.getQuantity() * capacity.getUnitVolume();

        // 2. Find available zones (Auto-Slotting)
        List<WarehouseZone> availableZones = warehouseZoneDAO.findAvailableZones(request.getAssetTypeId(), totalRequiredVolume);
        if (availableZones.isEmpty()) {
            throw new RuntimeException("No suitable zone found with sufficient capacity.");
        }

        WarehouseZone targetZone = availableZones.get(0);

        // 3. Create assets and place them (Simplifying to one placement for total quantity space)
        // In reality, we would loop and create multiple asset records.
        for (int i = 0; i < request.getQuantity(); i++) {
            Asset asset = new Asset();
            asset.setAssetName("Asset from PO " + request.getPurchaseOrderId());
            asset.setAssetTypeId(request.getAssetTypeId());
            asset.setCurrentStatus(AssetStatus.AVAILABLE);
            asset.setAcquisitionDate(java.time.LocalDate.now());
            // ... set other fields from order details if needed
            assetDAO.insert(asset);
            
            // Assume we can get the generated asset ID. 
            // Note: Our AssetDAO.insert might need to return the ID or we fetch it.
            // For now, let's assume we have it.
            
            // Record placement
            AssetPlacement placement = new AssetPlacement();
            placement.setAssetId(asset.getAssetId()); // This needs to be populated after insert
            placement.setZoneId(targetZone.getZoneId());
            placement.setPlacedBy(1); // TODO: Get current user
            placement.setPlacedAt(LocalDateTime.now());
            assetPlacementDAO.insert(placement);

            // Record transaction
            WarehouseTransaction tx = new WarehouseTransaction();
            tx.setAssetId(asset.getAssetId());
            tx.setZoneId(targetZone.getZoneId());
            tx.setTransactionType("INBOUND");
            tx.setExecutedBy(1); // TODO: Get current user
            tx.setExecutedAt(LocalDateTime.now());
            tx.setNote(request.getNote());
            int txId = warehouseTransactionDAO.insert(tx);

            // Map PO
            warehouseTransactionDAO.mapPoTransaction(request.getPurchaseOrderId(), txId);
        }

        // 4. Update Zone capacity
        targetZone.setCurrentCapacity(targetZone.getCurrentCapacity() + totalRequiredVolume);
        if (targetZone.getAssetTypeId() == null) {
            targetZone.setAssetTypeId(request.getAssetTypeId());
        }
        warehouseZoneDAO.update(targetZone);
    }

    @Override
    @Transactional
    public void processReturnInbound(InboundReturnRequest request) {
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
        placement.setPlacedBy(1);
        placement.setPlacedAt(LocalDateTime.now());
        assetPlacementDAO.insert(placement);

        // Record transaction
        WarehouseTransaction tx = new WarehouseTransaction();
        tx.setAssetId(asset.getAssetId());
        tx.setZoneId(targetZone.getZoneId());
        tx.setTransactionType("INBOUND");
        tx.setExecutedBy(1);
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
}
