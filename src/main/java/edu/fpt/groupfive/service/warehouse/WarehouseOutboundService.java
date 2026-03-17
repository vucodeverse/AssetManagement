package edu.fpt.groupfive.service.warehouse;

public interface WarehouseOutboundService {
    void processAllocationOutbound(Integer allocationRequestId, Integer assetId);
}
