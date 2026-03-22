package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.service.AssetHandoverService;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseOutboundServiceImpl implements WarehouseOutboundService {

    private final AssetHandoverService assetHandoverService;
    private final DepartmentService departmentService;

    @Override
    public List<HandoverResponseDTO> getPendingAllocations() {
        // Gọi service khác để lấy tất cả lệnh cấp phát
        List<AssetHandoverResponse> allAllocations = assetHandoverService.getAllByAllocation();

        // Lọc các lệnh có trạng thái PENDING
        return allAllocations.stream()
                .filter(a -> a.getStatus() == Status.PENDING)
                .map(this::mapToHandoverResponseDTO)
                .toList();
    }

    private HandoverResponseDTO mapToHandoverResponseDTO(AssetHandoverResponse assetHandover) {
        String fromDeptName = null;
        if (assetHandover.getFromDepartmentId() != null) {
            DepartmentResponse fromDept = departmentService.getDepartById(assetHandover.getFromDepartmentId());
            if (fromDept != null) {
                fromDeptName = fromDept.getDepartmentName();
            }
        }

        String toDeptName = null;
        if (assetHandover.getToDepartmentId() != null) {
            DepartmentResponse toDept = departmentService.getDepartById(assetHandover.getToDepartmentId());
            if (toDept != null) {
                toDeptName = toDept.getDepartmentName();
            }
        }

        return HandoverResponseDTO.builder()
                .handoverId(assetHandover.getHandoverId())
                .fromDepartmentName(fromDeptName)
                .toDepartmentName(toDeptName)
                .createdAt(assetHandover.getCreatedAt())
                .status(assetHandover.getStatus().name())
                .build();
    }
}
