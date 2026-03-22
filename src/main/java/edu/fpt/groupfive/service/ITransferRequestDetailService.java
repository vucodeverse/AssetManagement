package edu.fpt.groupfive.service;

import edu.fpt.groupfive.model.TransferRequestDetail;

import java.util.List;
import java.util.Optional;

public interface ITransferRequestDetailService {

    void updateNote(int detailId, String note);

    Optional<TransferRequestDetail> getDetailById(int detailId);

    List<TransferRequestDetail> getDetailsByTransferId(int transferId);
}
