package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.model.warehouse.InventoryVoucher;
import edu.fpt.groupfive.service.*;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WarehouseOutboundServiceImpl implements WarehouseOutboundService {

    private final AssetHandoverService assetHandoverService;
    private final DepartmentService departmentService;
    private final AllocationRequestService allocationRequestService;
    private final AssetTypeService assetTypeService;
    private final WhTransactionDAO whTransactionDAO;
    private final UserDAO userDAO;

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

    @Override
    public void confirmOutbound(Integer handoverId, Map<Integer, Integer> assets, String username, String note) {
        Integer executedBy = userDAO.findUserIdByUsername(username);
        
        // Prepare Voucher
        InventoryVoucher voucher = InventoryVoucher.builder()
                .voucherCode("XK-" + System.currentTimeMillis())
                .voucherType("OUTBOUND")
                .handoverId(handoverId)
                .createdBy(executedBy)
                .status("COMPLETED")
                .note(note)
                .build();
        
        // DAO handles:
        // 1. Create wh_inventory_vouchers
        // 2. For each asset:
        //    - Create wh_inventory_voucher_details
        //    - Create asset_handover_detail (Link asset to handover)
        //    - Update asset status to 'ASSIGNED' and department_id
        //    - Update Zone capacity
        //    - Delete wh_asset_placement
        //    - Insert wh_transactions
        // 3. Update asset_handover status to 'COMPLETED'
        whTransactionDAO.executeOutboundForHandover(voucher, assets);
        
        // Link to AllocationRequest status if exists
        AssetHandoverResponse handover = assetHandoverService.getHandoverById(handoverId);
        if (handover != null && handover.getAllocationRequestId() != null) {
            assetHandoverService.updateAllocationStatus(handover.getAllocationRequestId(), "COMPLETED");
        }
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
