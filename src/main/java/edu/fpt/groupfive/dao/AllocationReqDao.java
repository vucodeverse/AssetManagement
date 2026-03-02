package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AllocationRequest;

import java.util.List;

public interface AllocationReqDao {

    List<AllocationRequest> findAll(Integer departmentId);

    AllocationRequest findById(Integer id);

    Integer insert(AllocationRequest request);

    void update(AllocationRequest request);

    void delete(Integer id);

    void updateStatus(Integer id, String status, Integer amApprovedBy, String reasonReject);
}
