package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.service.warehouse.WhTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhTransactionServiceImpl implements WhTransactionService {

    private final WhTransactionDAO whTransactionDAO;

    @Override
    public List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter) {
        return whTransactionDAO.getAllTransactions(filter);
    }
}
