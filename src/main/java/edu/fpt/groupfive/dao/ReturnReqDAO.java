package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.ReturnRequest;

import java.time.LocalDate;
import java.util.List;

public interface ReturnReqDAO {
    List<ReturnRequest> findAll(Integer departmentId);
    ReturnRequest findById(Integer departmentId);
    Integer insert(ReturnRequest request);
    void update(ReturnRequest request);
    void delete(Integer id);
    void updateStatus(Integer id, String status, Integer whConfirmedBy);
    List<ReturnRequest> search(Integer departmentId, String keyword,
                               String status, LocalDate fromDate, LocalDate toDate);
}
