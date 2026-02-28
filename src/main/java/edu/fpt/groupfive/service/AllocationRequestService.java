package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.model.AllocationRequest;


import java.util.List;

public interface AllocationRequestService {
    List<AllocationRequest> getAllAllocationRequest(Integer departmentId);
    void createRequest(AllocationRequestCreateRequest dto);
}
