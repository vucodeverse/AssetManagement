package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;

import java.util.List;

public interface WhTransactionService {
    List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter);
}
