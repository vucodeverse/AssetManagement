package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequest {
    private Integer assetId;
    private Integer ticketId;
    private String transactionType; // IN, OUT, MOVE
    private Integer fromZoneId;
    private Integer toZoneId;
    private Integer performerId;
}
