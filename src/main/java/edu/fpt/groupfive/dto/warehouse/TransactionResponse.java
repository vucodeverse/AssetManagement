package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Integer id;
    private Integer assetId;
    private Integer ticketId;
    private String transactionType;
    private Integer fromZoneId;
    private Integer toZoneId;
    private Integer performerId;
    private LocalDateTime transactionDate;
}
