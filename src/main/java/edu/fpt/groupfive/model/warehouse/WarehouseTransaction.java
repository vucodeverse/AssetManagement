package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseTransaction {
    private Integer transactionId;
    private Integer assetId;
    private Integer zoneId;
    private String transactionType; // 'INBOUND' or 'OUTBOUND'
    private Integer executedBy;
    private LocalDateTime executedAt;
    private String note;
}
