package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketDetailResponse;
import edu.fpt.groupfive.dto.warehouse.TicketResponse;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryTicketMapper {

    public TicketResponse toResponse(InventoryTicket ticket, List<TicketDetail> details) {
        if (ticket == null)
            return null;

        List<TicketDetailResponse> detailResponses = new ArrayList<>();
        if (details != null) {
            for (TicketDetail detail : details) {
                detailResponses.add(toDetailResponse(detail));
            }
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .warehouseId(ticket.getWarehouseId())
                .ticketType(ticket.getTicketType())
                .status(ticket.getStatus())
                .createdBy(ticket.getCreatedBy())
                .createdAt(ticket.getCreatedAt())
                .completedAt(ticket.getCompletedAt())
                .note(ticket.getNote())
                .details(detailResponses)
                .build();
    }

    public TicketDetailResponse toDetailResponse(TicketDetail detail) {
        if (detail == null)
            return null;
        return TicketDetailResponse.builder()
                .id(detail.getId())
                .ticketId(detail.getTicketId())
                .assetTypeId(detail.getAssetTypeId())
                .expectedQuantity(detail.getExpectedQuantity())
                .actualQuantity(detail.getActualQuantity())
                .note(detail.getNote())
                .build();
    }
}
