package edu.fpt.groupfive.dao.warehouse;

import java.util.List;
import java.util.Map;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;

public interface WhTransactionDAO {
    record AssetPlacementPlan(
            Integer assetTypeId, 
            String assetTypeName, 
            Integer poDetailId, 
            java.math.BigDecimal price, 
            Integer targetZoneId, 
            Integer unitVolume
    ) {}

    Map<Integer, List<Integer>> executeInboundTransaction(Integer poId, Integer executedBy, List<AssetPlacementPlan> placements);

    List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter);
}
