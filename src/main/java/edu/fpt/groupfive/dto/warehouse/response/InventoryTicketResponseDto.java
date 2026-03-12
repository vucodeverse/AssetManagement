package edu.fpt.groupfive.dto.warehouse.response;

import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.TicketType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTicketResponseDto {
    private Integer id;
    private Integer warehouseId;
    private TicketType ticketType;
    private HandleStatus status;
    private Integer handleBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
