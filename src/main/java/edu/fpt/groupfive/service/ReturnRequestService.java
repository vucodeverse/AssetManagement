package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.model.ReturnRequest;

import java.util.List;

public interface ReturnRequestService {
    List<ReturnRequest> getAllRequest(Integer departmentId);
    ReturnRequest getRequestById (Integer id);
    void createRequest(ReturnRequestCreateRequest dto);
}
