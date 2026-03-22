package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.service.*;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseOutboundServiceImpl implements WarehouseOutboundService {

    private final AssetHandoverService assetHandoverService;
    private final DepartmentService departmentService;
    private final AllocationRequestService allocationRequestService;
    private final AssetTypeService assetTypeService;

    @Override
    public List<HandoverResponseDTO> getPendingAllocations() {
        List<AssetHandoverResponse> allAllocations = assetHandoverService.getAllByAllocation();

        return allAllocations.stream()
                .filter(a -> a.getStatus() == Status.PENDING)
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

        return HandoverDetailResponseDTO.builder()
                .handoverId(assetHandover.getHandoverId())
                .fromDepartmentName(getDeptName(assetHandover.getFromDepartmentId()))
                .toDepartmentName(getDeptName(assetHandover.getToDepartmentId()))
                .status(assetHandover.getStatus().name())
                .allocationRequest(allocationRequest)
                .requestedItems(requestedItems)
                .items(allocatedItems)
                .build();
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
