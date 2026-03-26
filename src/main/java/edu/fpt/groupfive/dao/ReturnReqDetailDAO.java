package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.model.ReturnRequestDetail;

import java.util.List;


public interface ReturnReqDetailDAO {
    void insertBatch(Integer requestId, List<ReturnRequestDetail> details);
    List<ReturnRequestDetail> findByRequestId(Integer requestId);
    void deleteByRequestId(Integer requestId);
}
