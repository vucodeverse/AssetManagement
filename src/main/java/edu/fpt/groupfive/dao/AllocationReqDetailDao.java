package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AllocationRequestDetail;

import java.util.List;

public interface AllocationReqDetailDao {
    void insertBatch(Integer requestId, List<AllocationRequestDetail> details);
}
