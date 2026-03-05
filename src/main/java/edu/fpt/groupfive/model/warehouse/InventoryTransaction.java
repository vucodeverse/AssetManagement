package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {
    private Integer id;
    private Integer assetId;
    private Integer ticketId; // Can be null
    private String transactionType; // IN, OUT, MOVE
    private Integer fromZoneId;
    private Integer toZoneId;
    private Integer performerId;
    private LocalDateTime transactionDate;
}
