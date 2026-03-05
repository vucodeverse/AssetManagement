package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTransactionDAO;
import edu.fpt.groupfive.dto.warehouse.TransactionCreateRequest;
import edu.fpt.groupfive.dto.warehouse.TransactionResponse;
import edu.fpt.groupfive.mapper.warehouse.InventoryTransactionMapper;
import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import edu.fpt.groupfive.service.warehouse.InventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    @Autowired
    private InventoryTransactionDAO transactionDAO;

    @Autowired
    private InventoryTransactionMapper transactionMapper;

    @Override
    public TransactionResponse logTransaction(TransactionCreateRequest request) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .assetId(request.getAssetId())
                .ticketId(request.getTicketId())
                .transactionType(request.getTransactionType())
                .fromZoneId(request.getFromZoneId())
                .toZoneId(request.getToZoneId())
                .performerId(request.getPerformerId())
                .build();

        transactionDAO.insert(transaction);

        // Fetch to get generated values (ID, date). Note: simplified fetch by assuming
        // latest tx for this asset is what we just inserted.
        List<InventoryTransaction> recentTx = transactionDAO.findByAssetId(request.getAssetId());
        InventoryTransaction savedTx = recentTx.get(0);

        return transactionMapper.toResponse(savedTx);
    }

    @Override
    public List<TransactionResponse> getTransactionsByAssetId(Integer assetId) {
        return transactionMapper.toResponseList(transactionDAO.findByAssetId(assetId));
    }

    @Override
    public List<TransactionResponse> getTransactionsByTicketId(Integer ticketId) {
        return transactionMapper.toResponseList(transactionDAO.findByTicketId(ticketId));
    }
}
