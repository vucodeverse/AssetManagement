package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.model.AllocationRequest;

import java.util.List;

public interface AllocationRequestService {
    List<AllocationRequest> getAllAllocationRequest(Integer departmentId);

    void createRequest(AllocationRequestCreateRequest dto);

    AllocationRequestResponse getRequestById(Integer id);

    void updateRequest(Integer id, AllocationRequestCreateRequest dto);

    void deleteRequest(Integer id);

    void updateStatus(Integer id, String status, Integer amApprovedBy, String reasonReject);
}
