package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;

import java.util.List;

public interface WhTransactionService {
    List<LedgerRecordResponseDTO> getAllTransactions();
}
