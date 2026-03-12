package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.dto.warehouse.request.TicketFormDto;
import edu.fpt.groupfive.dto.warehouse.response.InventoryTicketResponseDto;
import edu.fpt.groupfive.dto.warehouse.response.TicketDetailResponseDto;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryTicketServiceImpl implements InventoryTicketService {

    private final InventoryTicketDAO ticketDAO;
    private final TicketDetailDAO detailDAO;
    private final AssetTypeDAO assetTypeDAO;

    private InventoryTicketResponseDto mapToTicketDto(InventoryTicket entity) {
        if (entity == null)
            return null;
        return new InventoryTicketResponseDto(
                entity.getId(),
                entity.getWarehouseId(),
                entity.getTicketType(),
                entity.getStatus(),
                entity.getHandleBy(),
                entity.getCreatedAt(),
                entity.getCompletedAt());
    }

    private TicketDetailResponseDto mapToDetailDto(TicketDetail entity) {
        if (entity == null)
            return null;
        String assetTypeName = "N/A";
        if (entity.getAssetTypeId() != null) {
            try {
                AssetType assetType = assetTypeDAO.findById(entity.getAssetTypeId());
                if (assetType != null && assetType.getTypeName() != null) {
                    assetTypeName = assetType.getTypeName();
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
        return new TicketDetailResponseDto(
                entity.getId(),
                entity.getTicketId(),
                entity.getAssetTypeId(),
                assetTypeName,
                entity.getQuantity(),
                entity.getNote());
    }

    @Override
    public List<InventoryTicketResponseDto> getTicketsByWarehouseId(Integer warehouseId) {
        return ticketDAO.findAllByWarehouseId(warehouseId).stream()
                .map(this::mapToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryTicketResponseDto getTicketById(Integer ticketId) {
        return mapToTicketDto(ticketDAO.findById(ticketId));
    }

    @Override
    public List<TicketDetailResponseDto> getDetailsByTicketId(Integer ticketId) {
        return detailDAO.findByTicketId(ticketId).stream()
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryTicketResponseDto createTicket(Integer warehouseId, Integer handleBy, TicketFormDto formDto) {
        InventoryTicket ticket = new InventoryTicket();
        ticket.setWarehouseId(warehouseId);
        ticket.setTicketType(formDto.getTicketType());
        ticket.setHandleBy(handleBy);
        ticket.setStatus(HandleStatus.INBOX);
        ticket.setCreatedAt(java.time.LocalDateTime.now());

        InventoryTicket savedTicket = ticketDAO.insert(ticket);

        List<TicketDetail> entities = formDto.getDetails().stream()
                .filter(d -> d.getAssetTypeId() != null && d.getQuantity() != null && d.getQuantity() > 0)
                .map(d -> {
                    TicketDetail td = new TicketDetail();
                    td.setTicketId(savedTicket.getId());
                    td.setAssetTypeId(d.getAssetTypeId());
                    td.setQuantity(d.getQuantity());
                    td.setNote(d.getNote());
                    return td;
                })
                .collect(Collectors.toList());

        if (entities != null && !entities.isEmpty()) {
            detailDAO.insertBatch(entities);
        }

        return mapToTicketDto(savedTicket);
    }

    @Override
    @Transactional
    public InventoryTicketResponseDto updateTicket(Integer ticketId, TicketFormDto formDto) {
        InventoryTicket existingTicket = ticketDAO.findById(ticketId);
        if (existingTicket == null || !existingTicket.getStatus().equals(HandleStatus.INBOX)) {
            throw new IllegalStateException("Ticket cannot be updated unless it is in INBOX status.");
        }

        existingTicket.setTicketType(formDto.getTicketType());
        ticketDAO.update(existingTicket);

        detailDAO.deleteByTicketId(existingTicket.getId());

        List<TicketDetail> entities = formDto.getDetails().stream()
                .filter(d -> d.getAssetTypeId() != null && d.getQuantity() != null && d.getQuantity() > 0)
                .map(d -> {
                    TicketDetail td = new TicketDetail();
                    td.setTicketId(existingTicket.getId());
                    td.setAssetTypeId(d.getAssetTypeId());
                    td.setQuantity(d.getQuantity());
                    td.setNote(d.getNote());
                    return td;
                })
                .collect(Collectors.toList());

        if (entities != null && !entities.isEmpty()) {
            detailDAO.insertBatch(entities);
        }

        return mapToTicketDto(existingTicket);
    }
}
