package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
import edu.fpt.groupfive.model.ReturnRequest;

import java.util.List;

public interface ReturnRequestService {
    List<ReturnRequestRespnse> getAllRequest(Integer departmentId);
    ReturnRequestRespnse getRequestById (Integer id);
    void createRequest(ReturnRequestCreateRequest dto);
    void updateRequest(Integer id, ReturnRequestCreateRequest dto);
}
