package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketCreateRequest;
import edu.fpt.groupfive.dto.warehouse.TicketResponse;

import java.util.List;

public interface InventoryTicketService {
    TicketResponse createTicket(TicketCreateRequest request);

    TicketResponse getTicketById(Integer id);

    List<TicketResponse> getTicketsByWarehouseId(Integer warehouseId);
}
