package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.dto.request.AllocationRequestDetailRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AllocationRequestMapper {
    AllocationRequest toAllocationRequest(AllocationRequestCreateRequest request);

    @Mapping(target = "requestId", ignore = true)
    @Mapping(target = "requestDetailId", ignore = true)
    AllocationRequestDetail toDetailAllocationRequest(AllocationRequestDetailRequest request);

    List<AllocationRequestDetail> toListAllocationRequestDetail(List<AllocationRequestDetailRequest> list);

    List<AllocationRequestResponse> toResponseList(List<AllocationRequest> allocationRequests);
}
