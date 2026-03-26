package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.warehouse.WhAssetCapacityDAO;
import edu.fpt.groupfive.dao.warehouse.WhReceiptDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import edu.fpt.groupfive.service.*;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.fpt.groupfive.model.warehouse.WhReceipt;

@Service
@RequiredArgsConstructor
public class WarehouseOutboundServiceImpl implements WarehouseOutboundService {

    private final AssetHandoverService assetHandoverService;
    private final DepartmentService departmentService;
    private final AllocationRequestService allocationRequestService;
    private final AssetTypeService assetTypeService;
    private final WhZoneService whZoneService;
    private final WhTransactionDAO whTransactionDAO;
    private final AssetService assetService;
    private final WhAssetCapacityDAO whAssetCapacityDAO;
    private final WhReceiptDAO whReceiptDAO;

    @Override
    public List<HandoverResponseDTO> getAllocations() {
        List<AssetHandoverResponse> allAllocations = assetHandoverService.getAllByAllocation();

        return allAllocations.stream()
                .filter(a -> a.getStatus() == Status.PENDING || a.getStatus() == Status.COMPLETED)
                .map(this::mapToHandoverResponseDTO)
                .toList();
    }

    @Override
    public HandoverDetailResponseDTO getHandoverDetail(Integer handoverId) {
        AssetHandoverResponse assetHandover = assetHandoverService.getHandoverById(handoverId);
        if (assetHandover == null) return null;

        AllocationRequestResponse allocationRequest = null;
        if (assetHandover.getAllocationRequestId() != null) {
            allocationRequest = allocationRequestService.getRequestById(assetHandover.getAllocationRequestId());
        }

        List<HandoverDetailResponseDTO.HandoverItemDTO> allocatedItems = assetHandoverService.getHandoverDetails(handoverId);
        List<HandoverDetailResponseDTO.RequestedItemDTO> requestedItems = getRequestedItems(handoverId, allocatedItems);
        List<edu.fpt.groupfive.model.warehouse.WhReceipt> receipts = whReceiptDAO.findByAssetHandoverId(handoverId);

        return HandoverDetailResponseDTO.builder()
                .handoverId(assetHandover.getHandoverId())
                .fromDepartmentName(getDeptName(assetHandover.getFromDepartmentId()))
                .toDepartmentName(getDeptName(assetHandover.getToDepartmentId()))
                .status(assetHandover.getStatus().name())
                .allocationRequest(allocationRequest)
                .requestedItems(requestedItems)
                .items(allocatedItems)
                .receipts(receipts)
                .build();
    }

    @Override
    @Transactional
    public boolean processScan(Integer handoverId, String assetCode, Integer executedBy) {
        // 0. Check Handover status
        AssetHandoverResponse handover = assetHandoverService.getHandoverById(handoverId);
        if (handover == null) {
            throw new RuntimeException("Lệnh bàn giao không tồn tại.");
        }
        if (handover.getStatus() != Status.PENDING) {
            throw new RuntimeException("Lệnh bàn giao này không ở trạng thái PENDING, không thể thực hiện quét mã.");
        }

        // 1. Find asset location (and verify it's in warehouse)
        AssetLocationResponseDTO location = whZoneService.findAssetLocation(assetCode);
        if (location == null || location.getZoneId() == null) {
            throw new RuntimeException("Tài sản không có trong kho hoặc mã không hợp lệ.");
        }

        Integer assetId = location.getAssetId();
        AssetDetailResponse assetDetail = assetService.getDetailById(assetId);

        // Validation 1: Tài sản có trong kho (AVAILABLE)
        if (assetDetail.getCurrentStatus() != AssetStatus.AVAILABLE) {
            throw new RuntimeException("Tài sản không ở trạng thái 'Sẵn sàng sử dụng' (AVAILABLE). Trạng thái hiện tại: " + assetDetail.getCurrentStatus());
        }

        // 2. Get requirements for this handover
        List<AllocationRequestDetailResponse> reqDetails = assetHandoverService.getAllAllocationReqByHandoverId(handoverId);
        
        // Validation 2: Tài sản đúng loại tài sản theo yêu cầu
        boolean isRequiredType = reqDetails.stream()
                .anyMatch(req -> req.getAssetTypeId().equals(assetDetail.getAssetTypeId()));
        
        if (!isRequiredType) {
            throw new RuntimeException("Tài sản này không thuộc loại yêu cầu trong lệnh cấp phát.");
        }

        // Validation 3: Kiểm tra số lượng đã gán để tránh vượt định mức
        List<HandoverDetailResponseDTO.HandoverItemDTO> allocatedItems = assetHandoverService.getHandoverDetails(handoverId);
        String assetTypeName = assetTypeService.findNameById(assetDetail.getAssetTypeId());
        
        long currentAllocatedCount = allocatedItems.stream()
                .filter(item -> item.getAssetTypeName().equals(assetTypeName))
                .count();
        
        int requestedQuantityForThisType = reqDetails.stream()
                .filter(req -> req.getAssetTypeId().equals(assetDetail.getAssetTypeId()))
                .mapToInt(AllocationRequestDetailResponse::getRequestedQuantity)
                .sum();
        
        if (currentAllocatedCount >= requestedQuantityForThisType) {
            throw new RuntimeException("Đã quét đủ số lượng cho loại tài sản '" + assetTypeName + "' (" + requestedQuantityForThisType + ").");
        }

        // --- All checks passed ---

        // 3. Update asset status to ALLOCATED
        assetService.updateStatus(assetId, AssetStatus.ALLOCATED);

        // 4. Execute outbound transaction
        whTransactionDAO.executeOutboundTransaction(handoverId, assetId, location.getZoneId(), executedBy, "Xuất kho cấp phát");

        // 5. Decrease zone capacity
        int unitVolume = whAssetCapacityDAO.findByAssetTypeId(assetDetail.getAssetTypeId())
                .map(AssetCapacity::getUnitVolume)
                .orElse(0); 
        
        if (unitVolume > 0) {
            whZoneService.decreaseCapacity(location.getZoneId(), unitVolume);
        }

        // 6. Add handover detail log
        assetHandoverService.addHandoverDetail(handoverId, assetId);

        // 7. Update Progress & Status for Handover & AllocationRequest
        return updateProgressAndStatus(handoverId, handover.getAllocationRequestId());
    }

    @Override
    public AssetDetailResponse validateAssetForOutbound(String assetCode, Integer handoverId, List<String> stagedCodes) {
        // 0. Check duplicate in current stage
        if (stagedCodes != null && stagedCodes.contains(assetCode)) {
            throw new RuntimeException("Tài sản này đã có trong danh sách chọn tạm thời.");
        }

        // 1. Find asset location (and verify it's in warehouse)
        AssetLocationResponseDTO location = whZoneService.findAssetLocation(assetCode);
        if (location == null || location.getZoneId() == null) {
            throw new RuntimeException("Tài sản không có trong kho hoặc mã không hợp lệ.");
        }

        Integer assetId = location.getAssetId();
        AssetDetailResponse assetDetail = assetService.getDetailById(assetId);

        // Validation 1: Tài sản có trong kho (AVAILABLE)
        if (assetDetail.getCurrentStatus() != AssetStatus.AVAILABLE) {
            throw new RuntimeException("Tài sản không ở trạng thái 'Sẵn sàng sử dụng' (AVAILABLE).");
        }

        // 2. Get requirements for this handover
        List<AllocationRequestDetailResponse> reqDetails = assetHandoverService.getAllAllocationReqByHandoverId(handoverId);
        
        // Validation 2: Tài sản đúng loại tài sản theo yêu cầu
        AllocationRequestDetailResponse matchedReq = reqDetails.stream()
                .filter(req -> req.getAssetTypeId().equals(assetDetail.getAssetTypeId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loại tài sản '" + assetDetail.getAssetTypeName() + "' không được yêu cầu trong lệnh này."));

        // Validation 3: Kiểm tra số lượng đã xuất (DB) + số lượng đang chọn (Staged) có vượt quá yêu cầu không
        List<HandoverDetailResponseDTO.HandoverItemDTO> dbAllocatedItems = assetHandoverService.getHandoverDetails(handoverId);
        long dbCount = dbAllocatedItems.stream()
                .filter(item -> item.getAssetTypeName().equals(assetDetail.getAssetTypeName()))
                .count();

        long stagedCount = 0;
        if (stagedCodes != null && !stagedCodes.isEmpty()) {
            List<AssetDetailResponse> stagedAssets = getAssetsByCodes(stagedCodes);
            stagedCount = stagedAssets.stream()
                    .filter(a -> a.getAssetTypeId().equals(assetDetail.getAssetTypeId()))
                    .count();
        }

        if (dbCount + stagedCount >= matchedReq.getRequestedQuantity()) {
            throw new RuntimeException("Đã chọn đủ số lượng cho loại '" + assetDetail.getAssetTypeName() + "' (" + matchedReq.getRequestedQuantity() + "). Không thể thêm nữa.");
        }

        return assetDetail;
    }

    @Override
    public List<AssetDetailResponse> getAssetsByCodes(List<String> assetCodes) {
        if (assetCodes == null || assetCodes.isEmpty()) {
            return new ArrayList<>();
        }
        return assetCodes.stream()
                .map(code -> {
                    AssetLocationResponseDTO loc = whZoneService.findAssetLocation(code);
                    if (loc != null) {
                        return assetService.getDetailById(loc.getAssetId());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void confirmOutbound(Integer handoverId, List<String> assetCodes, Integer executedBy) {
        if (assetCodes == null || assetCodes.isEmpty()) {
            throw new RuntimeException("Danh sách tài sản xuất kho trống.");
        }

        AssetHandoverResponse handover = assetHandoverService.getHandoverById(handoverId);
        if (handover == null || handover.getStatus() != Status.PENDING) {
            throw new RuntimeException("Lệnh bàn giao không hợp lệ hoặc đã hoàn tất.");
        }

        // 1. Create Receipt
        WhReceipt receipt = WhReceipt.builder()
                .receiptNo(whReceiptDAO.generateNextReceiptNo("OUTBOUND"))
                .receiptType("OUTBOUND_ALLOCATION")
                .assetHandoverId(handoverId)
                .createdBy(executedBy)
                .note("Xuất kho cấp phát cho lệnh #" + handoverId)
                .build();
        
        int receiptId = whReceiptDAO.createReceipt(receipt);

        // 2. Process each asset
        for (String code : assetCodes) {
            AssetLocationResponseDTO location = whZoneService.findAssetLocation(code);
            int assetId = location.getAssetId();
            AssetDetailResponse assetDetail = assetService.getDetailById(assetId);

            // Update status
            assetService.updateStatus(assetId, AssetStatus.ALLOCATED);

            // Transaction
            whTransactionDAO.executeOutboundTransactionWithReceipt(receiptId, assetId, location.getZoneId(), executedBy, "Xuất kho cấp phát");

            // Capacity
            int unitVolume = whAssetCapacityDAO.findByAssetTypeId(assetDetail.getAssetTypeId())
                    .map(AssetCapacity::getUnitVolume)
                    .orElse(1);
            whZoneService.decreaseCapacity(location.getZoneId(), unitVolume);

            // Relate to handover
            assetHandoverService.addHandoverDetail(handoverId, assetId);
        }

        // 3. Update Status
        updateProgressAndStatus(handoverId, handover.getAllocationRequestId());
    }

    private boolean updateProgressAndStatus(Integer handoverId, Integer allocationId) {
        if (allocationId == null) return false;

        List<HandoverDetailResponseDTO.HandoverItemDTO> allocatedItems = assetHandoverService.getHandoverDetails(handoverId);
        List<AllocationRequestDetailResponse> reqDetails = assetHandoverService.getAllAllocationReqByHandoverId(handoverId);

        int totalRequested = reqDetails.stream().mapToInt(AllocationRequestDetailResponse::getRequestedQuantity).sum();
        int totalAllocated = allocatedItems.size();

        if (totalAllocated > 0) {
            if (totalAllocated < totalRequested) {
                assetHandoverService.updateAllocationStatus(allocationId, "IN_PROGRESS");
                return false;
            } else {
                // Fully allocated
                assetHandoverService.updateAllocationStatus(allocationId, "COMPLETED");
                assetHandoverService.updateStatus(handoverId, Status.APPROVED); // Mark handover as ready
                return true;
            }
        }
        return false;
    }

    private List<HandoverDetailResponseDTO.RequestedItemDTO> getRequestedItems(
            Integer handoverId, 
            List<HandoverDetailResponseDTO.HandoverItemDTO> allocatedItems) {
        
        List<AllocationRequestDetailResponse> reqDetails = assetHandoverService.getAllAllocationReqByHandoverId(handoverId);
        
        return reqDetails.stream().map(d -> {
            String typeName = assetTypeService.findNameById(d.getAssetTypeId());
            int allocatedCount = (int) allocatedItems.stream()
                    .filter(item -> item.getAssetTypeName().equals(typeName))
                    .count();
                    
            return HandoverDetailResponseDTO.RequestedItemDTO.builder()
                    .assetTypeName(typeName)
                    .requestedQuantity(d.getRequestedQuantity())
                    .allocatedQuantity(allocatedCount)
                    .build();
        }).toList();
    }

    private String getDeptName(Integer deptId) {
        if (deptId == null) return null;
        DepartmentResponse dept = departmentService.getDepartById(deptId);
        return dept != null ? dept.getDepartmentName() : null;
    }

    private HandoverResponseDTO mapToHandoverResponseDTO(AssetHandoverResponse assetHandover) {
        return HandoverResponseDTO.builder()
                .handoverId(assetHandover.getHandoverId())
                .fromDepartmentName(getDeptName(assetHandover.getFromDepartmentId()))
                .toDepartmentName(getDeptName(assetHandover.getToDepartmentId()))
                .createdAt(assetHandover.getCreatedAt())
                .status(assetHandover.getStatus().name())
                .build();
    }
}
