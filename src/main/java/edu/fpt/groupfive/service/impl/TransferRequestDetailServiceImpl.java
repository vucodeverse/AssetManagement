package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.TransferRequestDetailDAO;
import edu.fpt.groupfive.model.TransferRequestDetail;
import edu.fpt.groupfive.service.ITransferRequestDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferRequestDetailServiceImpl implements ITransferRequestDetailService {

    private final TransferRequestDetailDAO transferRequestDetailDAO;
    private final AssetDAO assetDAO;

    @Override
    public void updateNote(int detailId, String note) {
        int rows = transferRequestDetailDAO.updateNote(detailId, note);
        if (rows == 0) {
            throw new RuntimeException("Không tìm thấy chi tiết điều chuyển với ID: " + detailId);
        }
    }

    @Override
    public Optional<TransferRequestDetail> getDetailById(int detailId) {
        return transferRequestDetailDAO.findById(detailId);
    }

    @Override
    public List<TransferRequestDetail> getDetailsByTransferId(int transferId) {
        return transferRequestDetailDAO.findByTransferId(transferId);
    }

}

