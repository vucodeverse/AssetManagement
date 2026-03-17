package edu.fpt.groupfive.dto.warehouse.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionHistoryResponse {
    private Integer transactionId;
    private Integer assetId;
    private String assetName;
    private Integer zoneId;
    private String zoneName;
    private String transactionType;
    private String executedByName;
    private LocalDateTime executedAt;
    private String note;
}
