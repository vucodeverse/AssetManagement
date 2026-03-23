package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.model.TransferRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferRequestDAO {
    int createTransferRequest(TransferRequest request);

    int updateStatus(int transferId, String status);

    Optional<TransferRequest> findById(int transferId);

    List<TransferRequest> findAll();

    void delete(int transferRequestId);


    void updateSenderConfirm(int transferId, int userId, LocalDateTime time);

    void updateReceiverConfirm(int transferId, int userId, LocalDateTime time);

    List<TransferRequest> findByFromDepartmentId(Integer fromDeptId);
    List<TransferRequest> findByToDepartmentId(Integer toDeptId);
    List<TransferRequest> findByStatus(String status);

    List<TransferRequest> search(TransferSearchCriteria criteria, int offset, int size, String sortField, String sortDir);
    int countSearch(TransferSearchCriteria criteria);

    List<TransferRequest> searchForDepartmentManager(int departmentId, TransferSearchCriteria criteria, int offset, int size, String sortField, String sortDir);
    int countForDepartmentManager(int departmentId, TransferSearchCriteria criteria);
}
