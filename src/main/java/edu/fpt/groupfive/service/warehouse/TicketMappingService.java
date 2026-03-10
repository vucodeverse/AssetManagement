package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.response.TicketMappingResponseDto;

public interface TicketMappingService {

    /**
     * Gets the full mapping overview for a given ticket.
     */
    TicketMappingResponseDto getMappingDetails(Integer ticketId);

    /**
     * Validates and maps a single scanned physical asset to the appropriate ticket
     * detail.
     * 
     * @return the TicketDetail ID it was mapped to.
     */
    Integer mapScannedAsset(Integer ticketId, Integer assetId);

    /**
     * Validates mapping completeness and triggers Core Flow (Allocation).
     */
    void validateAndSubmitTicket(Integer ticketId);
}
