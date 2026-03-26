package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InventoryVoucherResponseDTO;
import edu.fpt.groupfive.model.warehouse.InventoryVoucher;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
import edu.fpt.groupfive.service.warehouse.WhAssetCapacityService;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseInboundServiceImpl implements WarehouseInboundService {

    private final OrderService orderService;
    private final WhTransactionDAO whTransactionDAO;
    private final UserDAO userDAO;
    private final WhZoneService whZoneService;
    private final WhAssetCapacityService whAssetCapacityService;

    @Override
    public InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username) {
        Integer executedBy = userDAO.findUserIdByUsername(username);
        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(request.getPurchaseOrderId());

        if (poDetail == null) {
            throw new RuntimeException("Không tìm thấy thông tin đơn mua hàng #" + request.getPurchaseOrderId());
        }

        // Fetch configs for placement
        List<ZoneCapacityResponseDTO> activeZones = whZoneService.getAllZones();
        List<AssetTypeVolumeDTO> assetVolumes = whAssetCapacityService.getAllAssetTypeVolumes();
        Map<Integer, Integer> unitVolumeMap = assetVolumes.stream()
                .filter(dto -> dto.getAssetTypeId() != null)
                .collect(Collectors.toMap(
                        AssetTypeVolumeDTO::getAssetTypeId,
                        dto -> dto.getUnitVolume() != null ? dto.getUnitVolume() : 1
                ));

        List<WhTransactionDAO.AssetPlacementPlan> placements = new ArrayList<>();
        Map<Integer, PurchaseOrderDetailResponse> orderDetailMap = poDetail.getOrderDetails().stream()
                .collect(Collectors.toMap(PurchaseOrderDetailResponse::getPurchaseOrderDetailId, d -> d));

        for (InboundRequestDTO.InboundItemRequestDTO itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) continue;

            PurchaseOrderDetailResponse orderDetail = orderDetailMap.get(itemReq.getPurchaseOrderDetailId());
            if (orderDetail == null) {
                throw new RuntimeException("Mục chi tiết PO #" + itemReq.getPurchaseOrderDetailId() + " không thuộc PO #" + request.getPurchaseOrderId());
            }

            // Validate remaining quantity
            int alreadyReceived = whTransactionDAO.getReceivedQuantity(itemReq.getPurchaseOrderDetailId());
            if (alreadyReceived + itemReq.getQuantity() > orderDetail.getQuantity()) {
                throw new RuntimeException("Số lượng nhập kho vượt quá số lượng còn lại của " + orderDetail.getAssetTypeName());
            }

            int assetTypeId = orderDetail.getAssetTypeId();
            int unitVolume = unitVolumeMap.getOrDefault(assetTypeId, 1);

            for (int i = 0; i < itemReq.getQuantity(); i++) {
                ZoneCapacityResponseDTO chosenZone = findZoneForAllocation(activeZones, assetTypeId, unitVolume);

                if (chosenZone == null) {
                    throw new RuntimeException("Kho đã đầy, không tìm thấy Zone phù hợp để xếp tài sản: " + orderDetail.getAssetTypeName());
                }

                // Update memory state for next asset in same batch
                chosenZone.setCurrentCapacity(chosenZone.getCurrentCapacity() + unitVolume);
                if (chosenZone.getAssetTypeId() == null) {
                    chosenZone.setAssetTypeId(assetTypeId);
                }

                placements.add(new WhTransactionDAO.AssetPlacementPlan(
                        assetTypeId,
                        orderDetail.getAssetTypeName(),
                        orderDetail.getPurchaseOrderDetailId(),
                        orderDetail.getPrice(),
                        chosenZone.getZoneId(),
                        unitVolume
                ));
            }
        }

        if (placements.isEmpty()) {
            throw new RuntimeException("Không có tài sản nào được chọn để nhập kho.");
        }

        // Prepare Voucher
        InventoryVoucher voucher = InventoryVoucher.builder()
                .voucherCode(request.getDeliveryNote() != null ? request.getDeliveryNote() : "NK-" + System.currentTimeMillis())
                .voucherType("INBOUND")
                .purchaseOrderId(request.getPurchaseOrderId())
                .createdBy(executedBy)
                .note(request.getNote())
                .status("COMPLETED")
                .build();

        // Execute batch database insertions
        WhTransactionDAO.ReceiptResult result = whTransactionDAO.executeInboundTransaction(voucher, placements);
        Map<Integer, List<Integer>> generatedIdsMap = result.generatedAssetIds();

        // Build Response Summary
        List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
        for (InboundRequestDTO.InboundItemRequestDTO itemReq : request.getItems()) {
             PurchaseOrderDetailResponse od = orderDetailMap.get(itemReq.getPurchaseOrderDetailId());
             if (itemReq.getQuantity() != null && itemReq.getQuantity() > 0 && generatedIdsMap.containsKey(od.getAssetTypeId())) {
                 groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                         .assetTypeName(od.getAssetTypeName())
                         .quantity(itemReq.getQuantity())
                         .assetIds(generatedIdsMap.get(od.getAssetTypeId()))
                         .build());
             }
        }

        return InboundSummaryResponseDTO.builder()
                .receiptId(result.voucherId())
                .purchaseOrderId(request.getPurchaseOrderId())
                .supplierName(poDetail.getSupplierName())
                .inboundDate(LocalDateTime.now())
                .assetGroups(groups)
                .build();
    }

    @Override
    public List<InventoryVoucher> getVouchersByOrderId(Integer orderId) {
        return whTransactionDAO.getVouchersByPoId(orderId);
    }

    @Override
    public InboundSummaryResponseDTO getInboundVoucherSummary(Integer voucherId) {
        return whTransactionDAO.getInboundReceiptSummary(voucherId);
    }

    @Override
    public List<InventoryVoucherResponseDTO> getAllInboundVouchers() {
        return whTransactionDAO.getAllInboundVouchers();
    }

    private ZoneCapacityResponseDTO findZoneForAllocation(List<ZoneCapacityResponseDTO> zones, int assetTypeId, int unitVolume) {
        ZoneCapacityResponseDTO bestFillUpZone = null;
        ZoneCapacityResponseDTO firstNewZone = null;

        for (ZoneCapacityResponseDTO zone : zones) {
            if ("ACTIVE".equals(zone.getStatus())) {
                boolean isSameAsset = zone.getAssetTypeId() != null && zone.getAssetTypeId() == assetTypeId;
                boolean isEmpty = zone.getAssetTypeId() == null || zone.getAssetTypeId() == 0;

                // Fill-up Strategy
                if (isSameAsset) {
                    if ((zone.getMaxCapacity() - zone.getCurrentCapacity()) >= unitVolume) {
                        if (bestFillUpZone == null || zone.getCurrentCapacity() > bestFillUpZone.getCurrentCapacity()) {
                            bestFillUpZone = zone;
                        }
                    }
                }
                // New Zone Strategy
                if (isEmpty && zone.getMaxCapacity() >= unitVolume) {
                    if (firstNewZone == null) {
                        firstNewZone = zone;
                    }
                }
            }
        }

        if (bestFillUpZone != null) return bestFillUpZone;
        return firstNewZone;
    }
}
