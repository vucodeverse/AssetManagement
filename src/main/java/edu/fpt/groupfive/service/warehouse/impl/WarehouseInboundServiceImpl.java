package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dao.warehouse.WhReceiptDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.AssetHandover;
import edu.fpt.groupfive.model.warehouse.WhReceipt;
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
    private final WhReceiptDAO whReceiptDAO; // Added
    private final UserDAO userDAO;
    private final AssetDAO assetDAO;
    private final WhZoneService whZoneService;
    private final WhAssetCapacityService whAssetCapacityService;
    private final AssetHandoverDao assetHandoverDao;
    private final AssetHandoverDetailDao assetHandoverDetailDao;
    private final ReturnReqDAO returnReqDAO;

    @Override
    public List<HandoverResponseDTO> getProcessedReturns() {
        List<AssetHandover> handovers = assetHandoverDao.findAllProcessedReturns();
        return handovers.stream().map(h -> HandoverResponseDTO.builder()
                .handoverId(h.getHandoverId())
                .fromDepartmentName(h.getFromDepartmentName())
                .toDepartmentName(h.getToDepartmentName())
                .createdAt(h.getCreatedAt())
                .status(h.getStatus().name())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<HandoverResponseDTO> getPendingReturns() {
        List<AssetHandover> handovers = assetHandoverDao.findAllPendingReturns();
        return handovers.stream().map(h -> HandoverResponseDTO.builder()
                .handoverId(h.getHandoverId())
                .fromDepartmentName(h.getFromDepartmentName())
                .toDepartmentName(h.getToDepartmentName())
                .createdAt(h.getCreatedAt())
                .status(h.getStatus().name())
                .build()).collect(Collectors.toList());
    }

    @Override
    public HandoverDetailResponseDTO getReturnDetail(Integer handoverId) {
        AssetHandover handover = assetHandoverDao.findById(handoverId);
        if (handover == null) {
            throw new RuntimeException("Không tìm thấy lệnh bàn giao #" + handoverId);
        }

        List<HandoverDetailResponseDTO.HandoverItemDTO> items = assetHandoverDetailDao
                .findItemsByHandoverId(handoverId);

        return HandoverDetailResponseDTO.builder()
                .handoverId(handoverId)
                .fromDepartmentName(handover.getFromDepartmentName()) // Note: findById doesn't join by default in
                                                                      // current impl, I might need to update it
                .toDepartmentName(handover.getToDepartmentName())
                .status(handover.getStatus().name())
                .items(items)
                .build();
    }

    @Override
    public InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username) {
        Integer executedBy = userDAO.findUserIdByUsername(username);
        Integer poId = request.getPoId();
        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);

        // 1. Create Receipt Master
        String receiptNo = whReceiptDAO.generateNextReceiptNo("INBOUND_PO");
        WhReceipt receipt = WhReceipt.builder()
                .receiptNo(receiptNo)
                .purchaseOrderId(poId)
                .receiptType("INBOUND_PO")
                .createdBy(executedBy)
                .note(request.getNote())
                .build();
        // 2. Prepare Placements (Do this first to validate capacity)
        List<ZoneCapacityResponseDTO> activeZones = whZoneService.getAllZones();
        List<AssetTypeVolumeDTO> assetVolumes = whAssetCapacityService.getAllAssetTypeVolumes();
        Map<Integer, Integer> unitVolumeMap = assetVolumes.stream()
                .filter(dto -> dto.getAssetTypeId() != null)
                .collect(Collectors.toMap(
                        AssetTypeVolumeDTO::getAssetTypeId,
                        dto -> dto.getUnitVolume() != null ? dto.getUnitVolume() : 1));

        List<WhTransactionDAO.AssetPlacementPlan> placements = new ArrayList<>();
        Map<Integer, PurchaseOrderDetailResponse> detailMap = poDetail.getOrderDetails().stream()
                .collect(Collectors.toMap(PurchaseOrderDetailResponse::getPurchaseOrderDetailId, d -> d));

        for (InboundRequestDTO.InboundItemRequestDTO reqItem : request.getItems()) {
            PurchaseOrderDetailResponse detail = detailMap.get(reqItem.getPoDetailId());
            if (detail == null) continue;

            int qtyToReceive = reqItem.getQuantityToReceive();
            if (qtyToReceive <= 0) continue;

            // Simple validation: already received + new <= ordered
            int alreadyReceived = detail.getReceivedQuantity() != null ? detail.getReceivedQuantity() : 0;
            if (alreadyReceived + qtyToReceive > detail.getQuantity()) {
                throw new RuntimeException("Số lượng nhập cho " + detail.getAssetTypeName() + " vượt quá số lượng đặt còn lại.");
            }

            int assetTypeId = detail.getAssetTypeId();
            int unitVolume = unitVolumeMap.getOrDefault(assetTypeId, 1);

            for (int i = 0; i < qtyToReceive; i++) {
                ZoneCapacityResponseDTO chosenZone = findZoneForAllocation(activeZones, assetTypeId, unitVolume);
                if (chosenZone == null) {
                    throw new RuntimeException("Kho đã đầy, không tìm thấy Zone phù hợp cho: " + detail.getAssetTypeName());
                }
                chosenZone.setCurrentCapacity(chosenZone.getCurrentCapacity() + unitVolume);
                if (chosenZone.getAssetTypeId() == null || chosenZone.getAssetTypeId() == 0) {
                    chosenZone.setAssetTypeId(assetTypeId);
                }
                placements.add(new WhTransactionDAO.AssetPlacementPlan(
                        assetTypeId,
                        detail.getAssetTypeName(),
                        detail.getPurchaseOrderDetailId(),
                        detail.getPrice(),
                        chosenZone.getZoneId(),
                        unitVolume));
            }
        }
        if (placements.isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số lượng nhận hợp lệ (lớn hơn 0) cho các mặt hàng.");
        }

        // 3. Create Receipt (Only if capacity was confirmed)
        int receiptId = whReceiptDAO.createReceipt(receipt);

        // 4. Execute Transactions
        Map<Integer, List<Integer>> generatedIds = whTransactionDAO.executeInboundTransaction(poId, executedBy, placements, receiptId);

        // 4. Update PO Status
        // Refresh PO to check if all completed
        PurchaseOrderResponse updatedPo = orderService.getPurchaseOrderById(poId);
        boolean allFinished = updatedPo.getOrderDetails().stream()
                .allMatch(d -> (d.getReceivedQuantity() != null ? d.getReceivedQuantity() : 0) >= d.getQuantity());

        if (allFinished) {
            orderService.updateStatus(poId, PurchaseProcessStatus.COMPLETED);
        } else {
            orderService.updateStatus(poId, PurchaseProcessStatus.PARTIALLY_RECEIVED);
        }

        // Build Response
        List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
        for (InboundRequestDTO.InboundItemRequestDTO reqItem : request.getItems()) {
            PurchaseOrderDetailResponse detail = detailMap.get(reqItem.getPoDetailId());
            if (detail != null && generatedIds.containsKey(detail.getAssetTypeId())) {
                groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                        .assetTypeName(detail.getAssetTypeName())
                        .quantity(reqItem.getQuantityToReceive())
                        .assetIds(generatedIds.get(detail.getAssetTypeId()))
                        .build());
            }
        }

        return InboundSummaryResponseDTO.builder()
                .purchaseOrderId(poId)
                .receiptId(receiptId)
                .receiptNo(receipt.getReceiptNo())
                .supplierName(poDetail.getSupplierName())
                .inboundDate(LocalDateTime.now())
                .assetGroups(groups)
                .build();
    }

    @Override
    @Deprecated
    public InboundSummaryResponseDTO processInboundPO(Integer poId, String username) {
        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);
        List<InboundRequestDTO.InboundItemRequestDTO> items = poDetail.getOrderDetails().stream()
                .map(d -> InboundRequestDTO.InboundItemRequestDTO.builder()
                        .poDetailId(d.getPurchaseOrderDetailId())
                        .quantityToReceive(d.getQuantity() - (d.getReceivedQuantity() != null ? d.getReceivedQuantity() : 0))
                        .build())
                .filter(i -> i.getQuantityToReceive() > 0)
                .collect(Collectors.toList());

        InboundRequestDTO request = InboundRequestDTO.builder()
                .poId(poId)
                .note("Nhập kho toàn bộ (Legacy)")
                .items(items)
                .build();

        return processInboundPO(request, username);
    }

    @Override
    public void processReturnScan(Integer handoverId, String assetCode, String username) {
        Integer executedBy = userDAO.findUserIdByUsername(username);
        Integer assetId;
        try {
            assetId = Integer.parseInt(assetCode);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Mã tài sản không hợp lệ: " + assetCode);
        }

        List<HandoverDetailResponseDTO.HandoverItemDTO> items = assetHandoverDetailDao.findItemsByHandoverId(handoverId);
        HandoverDetailResponseDTO.HandoverItemDTO match = items.stream()
                .filter(i -> i.getAssetId().equals(assetId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tài sản #" + assetId + " không thuộc lệnh thu hồi này."));

        if (match.isScanned()) {
            throw new RuntimeException("Tài sản #" + assetId + " đã được quét và nhận kho trước đó.");
        }

        Asset asset = assetDAO.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài sản #" + assetId));

        List<AssetTypeVolumeDTO> assetVolumes = whAssetCapacityService.getAllAssetTypeVolumes();
        Integer unitVolume = assetVolumes.stream()
                .filter(v -> v.getAssetTypeId().equals(asset.getAssetTypeId()))
                .map(AssetTypeVolumeDTO::getUnitVolume)
                .findFirst()
                .orElse(1);

        List<ZoneCapacityResponseDTO> zones = whZoneService.getAllZones();
        ZoneCapacityResponseDTO targetZone = findZoneForAllocation(zones, asset.getAssetTypeId(), unitVolume);

        if (targetZone == null) {
            throw new RuntimeException("Không còn đủ không gian cho loại tài sản này.");
        }

        // Create Receipt for Return (One receipt per scan session probably? For now one per asset is fine or we group later)
        // Actually, scan is usually one by one. Let's create one receipt if not exists for this handover session?
        // To simplify, let's create a receipt per return session or just link to a generic receipt.
        // Better: create/get a receipt for this handover.
        
        String receiptNo = "PN-RET-" + handoverId + "-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        WhReceipt receipt = WhReceipt.builder()
                .receiptNo(receiptNo)
                .assetHandoverId(handoverId)
                .receiptType("INBOUND_RETURN")
                .createdBy(executedBy)
                .note("Nhập kho từ lệnh thu hồi #" + handoverId)
                .build();
        int receiptId = whReceiptDAO.createReceipt(receipt);

        whTransactionDAO.executeReturnInboundTransaction(handoverId, assetId, targetZone.getZoneId(), executedBy, null, receiptId);

        List<HandoverDetailResponseDTO.HandoverItemDTO> updatedItems = assetHandoverDetailDao.findItemsByHandoverId(handoverId);
        boolean allScanned = updatedItems.stream().allMatch(HandoverDetailResponseDTO.HandoverItemDTO::isScanned);
        if (allScanned) {
            assetHandoverDao.updateStatus(handoverId, Status.COMPLETED);
            AssetHandover handover = assetHandoverDao.findById(handoverId);
            if (handover != null && handover.getReturnRequestId() != null) {
                returnReqDAO.updateStatus(handover.getReturnRequestId(), Status.COMPLETED.name(), executedBy);
            }
        }
    }

    private ZoneCapacityResponseDTO findZoneForAllocation(List<ZoneCapacityResponseDTO> zones, int assetTypeId,
            int unitVolume) {
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

        // Biased towards Fill-up first
        if (bestFillUpZone != null) {
            return bestFillUpZone;
        }
        return firstNewZone;
    }

    @Override
    public HandoverResponseDTO getReturnHandover(Integer handoverId) {
        AssetHandover handover = assetHandoverDao.findById(handoverId);
        if (handover == null) {
            throw new RuntimeException("Không tìm thấy lệnh thu hồi #" + handoverId);
        }

        return HandoverResponseDTO.builder()
                .handoverId(handover.getHandoverId())
                .fromDepartmentName(handover.getFromDepartmentName())
                .toDepartmentName(handover.getToDepartmentName())
                .createdAt(handover.getCreatedAt())
                .status(handover.getStatus().name())
                .build();
    }
    @Override
    public List<WhReceipt> getReceiptsByPOId(Integer poId) {
        return whReceiptDAO.findByPurchaseOrderId(poId);
    }
}
