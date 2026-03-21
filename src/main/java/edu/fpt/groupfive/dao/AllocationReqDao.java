package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AllocationRequest;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AllocationReqDao {

    List<AllocationRequest> findAll(Integer departmentId);

    AllocationRequest findById(Integer id);

    Integer insert(AllocationRequest request);

    void update(AllocationRequest request);

    void delete(Integer id);

    void updateStatus(Integer id, String status, Integer amApprovedBy, String reasonReject);

    List<AllocationRequest> search(Integer departmentId, String requestId, String status,
                                   String priority, LocalDate fromDate, LocalDate toDate);


    List<AllocationRequest> findAll();
    List<AllocationRequest> findAllPending();
    List<AllocationRequest> findAllOrderByCreatedAtDesc();
}
