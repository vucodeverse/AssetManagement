package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;

import edu.fpt.groupfive.dto.warehouse.response.InventoryTicketResponseDto;
import edu.fpt.groupfive.dto.warehouse.response.TicketDetailResponseDto;

import java.util.List;

public interface InventoryTicketService {
    List<InventoryTicketResponseDto> getTicketsByWarehouseId(Integer warehouseId);

    InventoryTicketResponseDto getTicketById(Integer ticketId);

    List<TicketDetailResponseDto> getDetailsByTicketId(Integer ticketId);

    InventoryTicketResponseDto createTicket(Integer warehouseId, Integer handleBy,
            edu.fpt.groupfive.dto.warehouse.request.TicketFormDto formDto);

    InventoryTicketResponseDto updateTicket(Integer ticketId,
            edu.fpt.groupfive.dto.warehouse.request.TicketFormDto formDto);
}
