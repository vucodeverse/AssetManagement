package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;

import java.util.List;

public interface InventoryTicketService {
    List<InventoryTicket> getTicketsByWarehouseId(Integer warehouseId);

    InventoryTicket getTicketById(Integer ticketId);

    List<TicketDetail> getDetailsByTicketId(Integer ticketId);

    InventoryTicket createTicket(InventoryTicket ticket, List<TicketDetail> details);

    InventoryTicket updateTicket(InventoryTicket ticket, List<TicketDetail> details);
}
