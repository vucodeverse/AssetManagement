package edu.fpt.groupfive.dao.warehouse;

import java.util.List;
import java.util.Map;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;

public interface WhTransactionDAO {
    Map<Integer, List<Integer>> processInboundPO(Integer poId, Integer executedBy, List<InboundAssetData> assetsToInbound);

    List<LedgerRecordResponseDTO> getAllTransactions();

    record InboundAssetData(Integer assetTypeId, String assetTypeName, Integer quantity, Integer poDetailId, java.math.BigDecimal price) {}
}
