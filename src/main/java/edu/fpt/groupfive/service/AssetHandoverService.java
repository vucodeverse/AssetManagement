package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.model.AssetHandover;

import java.util.List;

public interface AssetHandoverService {
    List<AllocationRequestDetailResponse> getAllAllocationReqByHandoverId(Integer handoverId);
    List<AssetHandoverResponse> getAllByAllocation();
    List<AssetHandoverResponse> getAllByReturn();

    AssetHandoverResponse getHandoverById(Integer id);

    List<HandoverDetailResponseDTO.HandoverItemDTO> getHandoverDetails(Integer handoverId);
}
