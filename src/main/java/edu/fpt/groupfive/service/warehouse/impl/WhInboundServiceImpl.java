package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.warehouse.WhPlacementDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.warehouse.InboundPOReceiveRequestDTO;
import edu.fpt.groupfive.dto.request.warehouse.POItemReceiveRequestDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.WhInboundService;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WhInboundServiceImpl implements WhInboundService {

    private final AssetService assetService;
    private final OrderService orderService;
    private final WhZoneService whZoneService;
    private final WhTransactionDAO whTransactionDAO;
    private final WhPlacementDAO whPlacementDAO;

    @Override
    @Transactional
    public void processPOInbound(InboundPOReceiveRequestDTO request, Integer userId) {
        Integer poId = request.getPurchaseOrderId();
        
        for (POItemReceiveRequestDTO item : request.getItems()) {
            if (item.getActualQuantity() <= 0) continue;

            // 1. Create Assets via AssetService
            AssetCreateRequest assetReq = new AssetCreateRequest();
            assetReq.setAssetTypeId(item.getAssetTypeId());
            assetReq.setQuantity(item.getActualQuantity());
            assetReq.setPurchaseOrderDetailId(item.getPurchaseOrderDetailId());
            // Set other default values appropriately
            assetReq.setAcquisitionDate(java.time.LocalDate.now());
            assetReq.setWarrantyStartDate(java.time.LocalDate.now());
            assetReq.setOriginalCost(java.math.BigDecimal.ZERO); // Ideally get from PO detail

            List<Integer> assetIds = assetService.create(assetReq);

            // 2. Find suitable zone
            WarehouseZone zone = whZoneService.findSuitableZone(item.getAssetTypeId(), item.getActualQuantity())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực kho phù hợp cho loại tài sản ID: " + item.getAssetTypeId()));

            // 3. Place assets and log transactions
            for (Integer assetId : assetIds) {
                whPlacementDAO.placeAsset(assetId, zone.getZoneId(), userId);

                WarehouseTransaction trans = new WarehouseTransaction();
                trans.setAssetId(assetId);
                trans.setZoneId(zone.getZoneId());
                trans.setTransactionType("INBOUND");
                trans.setExecutedBy(userId);
                trans.setExecutedAt(LocalDateTime.now());
                trans.setNote("Nhập kho từ PO #" + poId);

                Integer transId = whTransactionDAO.insert(trans);
                whTransactionDAO.linkPOToTransaction(poId, transId);
            }

            // 4. Update zone capacity
            whZoneService.updateCapacity(zone.getZoneId(), item.getActualQuantity(), item.getAssetTypeId());
        }

        // 5. Update PO status (Simplification: mark as COMPLETED for now)
        // In a real system, you'd check if all items are fully received.
        orderService.updateOrderStatus(poId, OrderStatus.COMPLETED);
    }
}
