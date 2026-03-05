package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.TransactionResponse;
import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryTransactionMapper {

    public TransactionResponse toResponse(InventoryTransaction transaction) {
        if (transaction == null)
            return null;
        return TransactionResponse.builder()
                .id(transaction.getId())
                .assetId(transaction.getAssetId())
                .ticketId(transaction.getTicketId())
                .transactionType(transaction.getTransactionType())
                .fromZoneId(transaction.getFromZoneId())
                .toZoneId(transaction.getToZoneId())
                .performerId(transaction.getPerformerId())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    public List<TransactionResponse> toResponseList(List<InventoryTransaction> transactions) {
        if (transactions == null)
            return new ArrayList<>();
        List<TransactionResponse> result = new ArrayList<>();
        for (InventoryTransaction tx : transactions) {
            result.add(toResponse(tx));
        }
        return result;
    }
}
