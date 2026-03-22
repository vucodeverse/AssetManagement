package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(Integer poId, String username);
}
