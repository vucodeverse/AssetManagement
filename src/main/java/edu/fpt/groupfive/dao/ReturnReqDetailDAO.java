package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.ReturnRequestDetail;

import java.util.List;


public interface ReturnReqDetailDAO {
    void insertBatch(Integer requestId, List<ReturnRequestDetail> details);
}
