package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AllocationRequest;

import java.util.List;

public interface AllocationReqDao {

    List<AllocationRequest> findAll(Integer departmentId);
    Integer insert(AllocationRequest request);
}
