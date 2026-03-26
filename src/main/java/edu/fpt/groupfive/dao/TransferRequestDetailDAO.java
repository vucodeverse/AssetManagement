package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.TransferRequestDetail;

import java.util.List;
import java.util.Optional;

public interface TransferRequestDetailDAO {
    void batchInsertDetails(int transferId, List<Integer> assetIds);

    void createDetail(TransferRequestDetail detail);

    int updateNote(int transferDetailId, String note);

    Optional<TransferRequestDetail> findById(int transferDetailId);

    List<TransferRequestDetail> findByTransferId(int transferId);
}
