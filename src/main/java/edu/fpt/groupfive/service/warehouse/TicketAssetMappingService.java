package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingRequest;
import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingResponse;

import java.util.List;

public interface TicketAssetMappingService {
    TicketAssetMappingResponse mapAssetToTicketDetail(TicketAssetMappingRequest request);

    List<TicketAssetMappingResponse> getMappingsByDetailId(Integer detailId);
}
