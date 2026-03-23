package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;

import java.util.List;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(Integer poId, String username);
    List<HandoverResponseDTO> getPendingReturns();
    List<HandoverResponseDTO> getProcessedReturns();
    HandoverDetailResponseDTO getReturnDetail(Integer handoverId);
}
