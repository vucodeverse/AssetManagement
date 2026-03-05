package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.dto.warehouse.TicketCreateRequest;
import edu.fpt.groupfive.dto.warehouse.TicketResponse;
import edu.fpt.groupfive.mapper.warehouse.InventoryTicketMapper;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryTicketServiceImpl implements InventoryTicketService {

    @Autowired
    private InventoryTicketDAO ticketDAO;

    @Autowired
    private TicketDetailDAO ticketDetailDAO;

    @Autowired
    private InventoryTicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        InventoryTicket ticket = InventoryTicket.builder()
                .warehouseId(request.getWarehouseId())
                .ticketType(request.getTicketType())
                .status("PENDING")
                .createdBy(request.getCreatedBy())
                .note(request.getNote())
                .build();

        // 1. Insert ticket
        ticketDAO.insert(ticket);

        // Due to plain JDBC without returning generated keys currently in DAO, we might
        // need a workaround
        // Ideally, the DAO insert method should return the generated ID.
        // For now, let's assume we can retrieve the latest ticket for this warehouse
        // This is a simplified approach. In reality, use KeyHolder in DAO to get the
        // generated ID reliably.
        List<InventoryTicket> latestTickets = ticketDAO.findByWarehouseId(request.getWarehouseId());
        InventoryTicket savedTicket = latestTickets.get(0); // Assuming DESC order in DAO

        List<TicketDetail> details = new ArrayList<>();

        if (request.getDetails() != null) {
            for (TicketCreateRequest.TicketDetailRequest detailReq : request.getDetails()) {
                TicketDetail detail = TicketDetail.builder()
                        .ticketId(savedTicket.getId())
                        .assetTypeId(detailReq.getAssetTypeId())
                        .expectedQuantity(detailReq.getExpectedQuantity())
                        .actualQuantity(0)
                        .note(detailReq.getNote())
                        .build();
                ticketDetailDAO.insert(detail);
                details.add(detail);
            }
        }

        return ticketMapper.toResponse(savedTicket, details);
    }

    @Override
    public TicketResponse getTicketById(Integer id) {
        InventoryTicket ticket = ticketDAO.findById(id);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }
        List<TicketDetail> details = ticketDetailDAO.findByTicketId(id);
        return ticketMapper.toResponse(ticket, details);
    }

    @Override
    public List<TicketResponse> getTicketsByWarehouseId(Integer warehouseId) {
        List<InventoryTicket> tickets = ticketDAO.findByWarehouseId(warehouseId);
        List<TicketResponse> responses = new ArrayList<>();
        for (InventoryTicket ticket : tickets) {
            List<TicketDetail> details = ticketDetailDAO.findByTicketId(ticket.getId());
            responses.add(ticketMapper.toResponse(ticket, details));
        }
        return responses;
    }
}
