package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.TransferAction;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.TransferResponse;
import edu.fpt.groupfive.model.TransferRequest;

import java.util.List;
import java.util.Optional;

public interface ITransferRequestService {

    TransferResponse createTransferRequest(TransferRequestCreate dto);

    void processTransferAction(int transferId, int userId, TransferAction action, Boolean issue);

//    TransferResponse createTransferRequest(TransferRequestCreate dto);
//
//    List<TransferRequest> findAll();
//
//    void updateStatus(int transferId, String status);
//
//    Optional<TransferRequest> getTransferRequestById(int transferId);


    List<TransferResponse> getTransfersForSender(int departmentId);
    List<TransferResponse> getTransfersForReceiver(int departmentId);
    List<TransferResponse> getTransfersForWarehouse();
    TransferResponse getTransferDetail(int transferId);

    List<TransferResponse> getTransfersForDepartmentManager(int departmentId);
}
