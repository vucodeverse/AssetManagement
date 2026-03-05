package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.TransactionCreateRequest;
import edu.fpt.groupfive.dto.warehouse.TransactionResponse;

import java.util.List;

public interface InventoryTransactionService {
    TransactionResponse logTransaction(TransactionCreateRequest request);

    List<TransactionResponse> getTransactionsByAssetId(Integer assetId);

    List<TransactionResponse> getTransactionsByTicketId(Integer ticketId);
}
