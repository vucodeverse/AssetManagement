package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.model.warehouse.WhReceipt;

import java.util.List;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username);
    @Deprecated
    InboundSummaryResponseDTO processInboundPO(Integer poId, String username);
    List<HandoverResponseDTO> getPendingReturns();
    List<HandoverResponseDTO> getProcessedReturns();
    HandoverDetailResponseDTO getReturnDetail(Integer handoverId);
    void processReturnScan(Integer handoverId, String assetCode, String username);
    List<WhReceipt> getReceiptsByPOId(Integer poId);
    HandoverResponseDTO getReturnHandover(Integer handoverId);
}
