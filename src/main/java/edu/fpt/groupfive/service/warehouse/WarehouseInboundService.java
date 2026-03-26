package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.model.warehouse.WhReceipt;

import edu.fpt.groupfive.dto.response.AssetDetailResponse;

import java.util.List;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username);
    @Deprecated
    InboundSummaryResponseDTO processInboundPO(Integer poId, String username);
    List<HandoverResponseDTO> getPendingReturns();
    List<HandoverResponseDTO> getProcessedReturns();
    List<HandoverResponseDTO> getAllReturns();
    HandoverDetailResponseDTO getReturnDetail(Integer handoverId);
    void processReturnScan(Integer handoverId, String assetCode, String username);
    List<WhReceipt> getReceiptsByPOId(Integer poId);
    HandoverResponseDTO getReturnHandover(Integer handoverId);
    InboundSummaryResponseDTO getReceiptSummary(Integer receiptId);

    /** Validate asset is eligible to be staged for this return inbound. */
    AssetDetailResponse validateAssetForReturnInbound(String assetCode, Integer handoverId, List<String> stagedCodes);

    /** Retrieve asset details by a list of asset ID codes for staging display. */
    List<AssetDetailResponse> getAssetsByCodes(List<String> assetCodes);

    /** Commit all staged assets: create one inbound receipt and process zone placement for all. */
    int confirmReturnInbound(Integer handoverId, List<String> assetCodes, String username);
}
