package edu.fpt.groupfive.service.impl.warehouse;

import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryTicketServiceImpl implements InventoryTicketService {

    private final InventoryTicketDAO ticketDAO;
    private final TicketDetailDAO detailDAO;

    @Override
    public List<InventoryTicket> getTicketsByWarehouseId(Integer warehouseId) {
        return ticketDAO.findAllByWarehouseId(warehouseId);
    }

    @Override
    public InventoryTicket getTicketById(Integer ticketId) {
        return ticketDAO.findById(ticketId);
    }

    @Override
    public List<TicketDetail> getDetailsByTicketId(Integer ticketId) {
        return detailDAO.findByTicketId(ticketId);
    }

    @Override
    @Transactional
    public InventoryTicket createTicket(InventoryTicket ticket, List<TicketDetail> details) {
        ticket.setStatus(HandleStatus.INBOX);
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(java.time.LocalDateTime.now());
        }

        InventoryTicket savedTicket = ticketDAO.insert(ticket);

        for (TicketDetail detail : details) {
            detail.setTicketId(savedTicket.getId());
        }
        if (details != null && !details.isEmpty()) {
            detailDAO.insertBatch(details);
        }

        return savedTicket;
    }

    @Override
    @Transactional
    public InventoryTicket updateTicket(InventoryTicket ticket, List<TicketDetail> details) {
        // Find existing ticket
        InventoryTicket existingTicket = ticketDAO.findById(ticket.getId());
        if (existingTicket == null || !existingTicket.getStatus().equals(HandleStatus.INBOX)) {
            throw new IllegalStateException("Ticket cannot be updated unless it is in INBOX status.");
        }

        // Update ticket main info
        existingTicket.setTicketType(ticket.getTicketType());
        ticketDAO.update(existingTicket);

        // Sync details: For simplicity, delete all old ones and insert new ones
        detailDAO.deleteByTicketId(existingTicket.getId());

        for (TicketDetail detail : details) {
            detail.setTicketId(existingTicket.getId());
        }
        if (details != null && !details.isEmpty()) {
            detailDAO.insertBatch(details);
        }

        return existingTicket;
    }
}
