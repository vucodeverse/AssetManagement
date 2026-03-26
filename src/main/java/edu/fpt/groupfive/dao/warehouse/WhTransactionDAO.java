package edu.fpt.groupfive.dao.warehouse;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;

public interface WhTransactionDAO {
    record AssetPlacementPlan(
            Integer assetTypeId, 
            String assetTypeName, 
            Integer poDetailId, 
            BigDecimal price, 
            Integer targetZoneId, 
            Integer unitVolume
    ) {}

    Map<Integer, List<Integer>> executeInboundTransaction(Integer poId, Integer executedBy, List<AssetPlacementPlan> placements, Integer receiptId);
    void executeReturnInboundTransaction(Integer handoverId, Integer assetId, Integer zoneId, Integer executedBy, String note, Integer receiptId);
    void executeOutboundTransaction(Integer handoverId, Integer assetId, Integer zoneId, Integer executedBy, String note);

    List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter);

    List<edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO.AssetGroupDTO> findAssetGroupsByReceiptId(Integer receiptId);
}
