package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.TransferAction;
import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.TransferResponse;
import edu.fpt.groupfive.model.TransferRequest;

import java.util.List;
import java.util.Optional;

public interface ITransferRequestService {

    TransferResponse createTransferRequest(TransferRequestCreate dto);

    void processTransferAction(int transferId, int userId, TransferAction action, Boolean issue);

    PageResponse<TransferResponse> searchForDepartmentManager(int departmentId, TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir);
    PageResponse<TransferResponse> searchForWarehouse(TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir);

    PageResponse<TransferResponse> searchForReceiver(
            int departmentId, TransferSearchCriteria criteria,
            int page, int size, String sortField, String sortDir);

    PageResponse<TransferResponse> searchByAssetManagerId(
            int assetManagerId, TransferSearchCriteria criteria,
            int page, int size, String sortField, String sortDir);

    PageResponse<TransferResponse> searchForAssetManager(TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir);


    List<TransferResponse> getTransfersForSender(int departmentId);
    List<TransferResponse> getTransfersForReceiver(int departmentId);
    List<TransferResponse> getAllTransfers();
    TransferResponse getTransferDetail(int transferId);

    List<TransferResponse> getTransfersForDepartmentManager(int departmentId);

    TransferRequest getTransferById(int transferId);
}
