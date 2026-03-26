package edu.fpt.groupfive.dao.warehouse;

import java.util.List;
import java.util.Map;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.model.warehouse.InboundReceipt;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;

public interface WhTransactionDAO {
    record AssetPlacementPlan(
            Integer assetTypeId, 
            String assetTypeName, 
            Integer poDetailId, 
            java.math.BigDecimal price, 
            Integer targetZoneId, 
            Integer unitVolume
    ) {}

    record ReceiptResult(Integer receiptId, Map<Integer, List<Integer>> generatedAssetIds) {}

    ReceiptResult executeInboundTransaction(InboundReceipt receipt, List<AssetPlacementPlan> placements);

    void executeOutboundTransaction(Integer handoverId, Integer assetId, Integer zoneId, Integer executedBy, String note);

    List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter);

    Integer getReceivedQuantity(Integer poDetailId);

    InboundSummaryResponseDTO getInboundReceiptSummary(Integer receiptId);

    List<InboundReceipt> getReceiptsByPoId(Integer poId);

    List<edu.fpt.groupfive.dto.response.warehouse.InboundReceiptResponseDTO> getAllInboundReceipts();
}
