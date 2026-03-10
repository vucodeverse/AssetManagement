package edu.fpt.groupfive.model.warehouse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InventoryTicket {
    private Integer id;
    private Integer warehouseId;
    private TicketType ticketType; //IN, OUT
    private HandleStatus status; // INBOX, PENDING, COMPLETED, CANCELLED
    private Integer handleBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
