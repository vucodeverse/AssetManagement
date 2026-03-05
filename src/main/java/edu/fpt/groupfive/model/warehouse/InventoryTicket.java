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
public class InventoryTicket {
    private Integer id;
    private Integer warehouseId;
    private String ticketType; // IN, OUT
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String note;
}
