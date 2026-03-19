package edu.fpt.groupfive.service.warehouse;

import java.util.List;
import java.util.Map;

public interface WarehouseOutboundService {
    void processAllocationOutbound(Integer allocationRequestId, Integer assetId);
    List<Map<String, Object>> getPendingAllocations();
}
