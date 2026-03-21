package edu.fpt.groupfive.dao.warehouse;

import java.util.List;

import java.util.Map;

public interface WhTransactionDAO {
    Map<Integer, List<Integer>> processInboundPO(Integer poId, Integer executedBy, List<InboundAssetData> assetsToInbound);

    record InboundAssetData(Integer assetTypeId, String assetTypeName, Integer quantity, Integer poDetailId, java.math.BigDecimal price) {}
}
