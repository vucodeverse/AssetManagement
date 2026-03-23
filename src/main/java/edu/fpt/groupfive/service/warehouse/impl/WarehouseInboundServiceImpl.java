package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dao.AssetHandoverDao;
import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.AssetHandover;
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
    private final AssetHandoverDao assetHandoverDao;
    private final AssetHandoverDetailDao assetHandoverDetailDao;

    @Override
    public List<HandoverResponseDTO> getProcessedReturns() {
        List<AssetHandover> handovers = assetHandoverDao.findAllProcessedReturns();
        return handovers.stream().map(h -> HandoverResponseDTO.builder()
                .handoverId(h.getHandoverId())
                .fromDepartmentName(h.getFromDepartmentName())
                .toDepartmentName(h.getToDepartmentName())
                .createdAt(h.getCreatedAt())
                .status(h.getStatus().name())
                .build()
        ).collect(Collectors.toList());
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
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public HandoverDetailResponseDTO getReturnDetail(Integer handoverId) {
        AssetHandover handover = assetHandoverDao.findById(handoverId);
        if (handover == null) {
            throw new RuntimeException("Không tìm thấy lệnh bàn giao #" + handoverId);
        }

        List<HandoverDetailResponseDTO.HandoverItemDTO> items = assetHandoverDetailDao.findItemsByHandoverId(handoverId);

        return HandoverDetailResponseDTO.builder()
                .handoverId(handoverId)
                .fromDepartmentName(handover.getFromDepartmentName()) // Note: findById doesn't join by default in current impl, I might need to update it
                .toDepartmentName(handover.getToDepartmentName())
                .status(handover.getStatus().name())
                .items(items)
                .build();
    }

    @Override
    public InboundSummaryResponseDTO processInboundPO(Integer poId, String username) {
        Integer executedBy = userDAO.findUserIdByUsername(username);

        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);

        // Fetch configs
        List<ZoneCapacityResponseDTO> activeZones = whZoneService.getAllZones();
        List<AssetTypeVolumeDTO> assetVolumes = whAssetCapacityService.getAllAssetTypeVolumes();
        Map<Integer, Integer> unitVolumeMap = assetVolumes.stream()
                .filter(dto -> dto.getAssetTypeId() != null)
                .collect(Collectors.toMap(
                        AssetTypeVolumeDTO::getAssetTypeId,
                        dto -> dto.getUnitVolume() != null ? dto.getUnitVolume() : 1
                ));

        List<WhTransactionDAO.AssetPlacementPlan> placements = new ArrayList<>();

        if (poDetail != null && poDetail.getOrderDetails() != null) {
            for (PurchaseOrderDetailResponse item : poDetail.getOrderDetails()) {
                if (item.getQuantity() == null || item.getQuantity() <= 0) continue;

                int quantity = item.getQuantity();
                int assetTypeId = item.getAssetTypeId();
                int unitVolume = unitVolumeMap.getOrDefault(assetTypeId, 1);

                for (int i = 0; i < quantity; i++) {
                    ZoneCapacityResponseDTO chosenZone = findZoneForAllocation(activeZones, assetTypeId, unitVolume);

                    if (chosenZone == null) {
                        throw new RuntimeException("Kho đã đầy, không tìm thấy Zone phù hợp để xếp tài sản: " + item.getAssetTypeName());
                    }

                    // Update memory state
                    chosenZone.setCurrentCapacity(chosenZone.getCurrentCapacity() + unitVolume);
                    if (chosenZone.getAssetTypeId() == null) {
                        chosenZone.setAssetTypeId(assetTypeId);
                    }

                    placements.add(new WhTransactionDAO.AssetPlacementPlan(
                            assetTypeId,
                            item.getAssetTypeName(),
                            item.getPurchaseOrderDetailId(),
                            item.getPrice(),
                            chosenZone.getZoneId(),
                            unitVolume
                    ));
                }
            }
        }

        // Execute batch database insertions
        Map<Integer, List<Integer>> generatedIds = whTransactionDAO.executeInboundTransaction(poId, executedBy, placements);

        // Build Response
        List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
        if (poDetail != null && poDetail.getOrderDetails() != null) {
            for (PurchaseOrderDetailResponse item : poDetail.getOrderDetails()) {
                if (item.getQuantity() == null) continue;
                int qty = item.getQuantity();
                if (qty > 0 && generatedIds.containsKey(item.getAssetTypeId())) {
                    groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                            .assetTypeName(item.getAssetTypeName())
                            .quantity(qty)
                            .assetIds(generatedIds.get(item.getAssetTypeId()))
                            .build());
                }
            }
        }

        return InboundSummaryResponseDTO.builder()
                .purchaseOrderId(poId)
                .supplierName(poDetail != null ? poDetail.getSupplierName() : null)
                .inboundDate(LocalDateTime.now())
                .assetGroups(groups)
                .build();
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

        // Biased towards Fill-up first
        if (bestFillUpZone != null) {
            return bestFillUpZone;
        }
        return firstNewZone;
    }
}
