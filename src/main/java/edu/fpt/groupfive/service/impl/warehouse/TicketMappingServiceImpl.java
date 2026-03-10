package edu.fpt.groupfive.service.impl.warehouse;

import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.TicketAssetMappingDAO;
import edu.fpt.groupfive.dao.warehouse.AssetLocationDAO;
import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.response.TicketDetailMappingDto;
import edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto;
import edu.fpt.groupfive.dto.warehouse.response.TicketMappingResponseDto;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import edu.fpt.groupfive.service.warehouse.TicketMappingService;
import edu.fpt.groupfive.service.warehouse.ZoneAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMappingServiceImpl implements TicketMappingService {

    private final InventoryTicketDAO inventoryTicketDAO;
    private final TicketAssetMappingDAO ticketAssetMappingDAO;
    private final AssetDAO assetDAO;
    private final ZoneAllocationService zoneAllocationService;
    private final ZoneDAO zoneDAO;
    private final AssetLocationDAO assetLocationDAO;

    @Override
    public TicketMappingResponseDto getMappingDetails(Integer ticketId) {
        InventoryTicket ticket = inventoryTicketDAO.findById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Phiếu kho không tồn tại.");
        }

        List<TicketDetailMappingDto> details = ticketAssetMappingDAO.getDetailMappingsByTicketId(ticketId);
        List<TicketMappedAssetDto> mappedAssets = ticketAssetMappingDAO.getMappedAssetsByTicketId(ticketId);

        int totalRequested = details.stream().mapToInt(TicketDetailMappingDto::getQuantityRequested).sum();
        int totalMapped = details.stream().mapToInt(TicketDetailMappingDto::getQuantityMapped).sum();
        int progressPercentage = totalRequested == 0 ? 0 : (totalMapped * 100) / totalRequested;

        TicketMappingResponseDto response = new TicketMappingResponseDto();
        response.setTicket(ticket);
        response.setDetails(details);
        response.setMappedAssets(mappedAssets);
        response.setTotalRequested(totalRequested);
        response.setTotalMapped(totalMapped);
        response.setProgressPercentage(progressPercentage);

        return response;
    }

    @Override
    @Transactional
    public Integer mapScannedAsset(Integer ticketId, Integer assetId) {
        InventoryTicket ticket = inventoryTicketDAO.findById(ticketId);
        if (ticket == null || ticket.getStatus() != HandleStatus.INBOX) {
            throw new RuntimeException("Phiếu không hợp lệ hoặc không ở trạng thái INBOX.");
        }

        Asset asset = assetDAO.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Tài sản ID " + assetId + " không tồn tại."));

        List<TicketMappedAssetDto> alreadyMapped = ticketAssetMappingDAO.getMappedAssetsByTicketId(ticketId);
        boolean isAlreadyMapped = alreadyMapped.stream().anyMatch(a -> a.getAssetId().equals(assetId));
        if (isAlreadyMapped) {
            throw new RuntimeException("Tài sản này đã được map vào phiếu.");
        }

        List<TicketDetailMappingDto> details = ticketAssetMappingDAO.getDetailMappingsByTicketId(ticketId);

        // Find a detail row that matches the asset type and still needs quantity
        TicketDetailMappingDto targetDetail = details.stream()
                .filter(d -> d.getAssetTypeId().equals(asset.getAssetTypeId())
                        && d.getQuantityMapped() < d.getQuantityRequested())
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Tài sản loại này không có trong yêu cầu hoặc đã đủ số lượng."));

        TicketAssetMapping mapping = new TicketAssetMapping();
        mapping.setDetailId(targetDetail.getDetailId());
        mapping.setAssetId(assetId);
        mapping.setUpdatedAt(LocalDateTime.now());

        ticketAssetMappingDAO.insert(mapping);

        return targetDetail.getDetailId();
    }

    @Override
    @Transactional
    public void validateAndSubmitTicket(Integer ticketId) {
        InventoryTicket ticket = inventoryTicketDAO.findById(ticketId);
        if (ticket == null || ticket.getStatus() != HandleStatus.INBOX) {
            throw new RuntimeException("Phiếu không tồn tại hoặc không hợp lệ để chốt.");
        }

        int unmatched = ticketAssetMappingDAO.countUnmatchedDetails(ticketId);
        if (unmatched > 0) {
            throw new RuntimeException("Phiếu chưa được map đủ tài sản yêu cầu. Vui lòng quét thêm tài sản.");
        }

        // Zone Allocation & Updates based on Ticket Type
        if ("IN".equals(ticket.getTicketType())) {
            zoneAllocationService.allocateAssetsForTicket(ticketId);
        } else if ("OUT".equals(ticket.getTicketType())) {
            zoneDAO.batchDecreaseCapacityByTicketId(ticketId);
            assetLocationDAO.deleteByTicketId(ticketId);
        }
        ticket.setStatus(HandleStatus.COMPLETED);
        inventoryTicketDAO.updateStatus(ticketId, HandleStatus.COMPLETED);
    }

    @Override
    @Transactional
    public void removeScannedAsset(Integer ticketId, Integer detailId, Integer assetId) {
        InventoryTicket ticket = inventoryTicketDAO.findById(ticketId);
        if (ticket == null || ticket.getStatus() != HandleStatus.INBOX) {
            throw new RuntimeException("Phiếu không hợp lệ hoặc không ở trạng thái INBOX.");
        }
        ticketAssetMappingDAO.deleteMapping(detailId, assetId);
    }
}
