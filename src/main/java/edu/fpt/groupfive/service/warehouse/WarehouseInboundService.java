package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.InboundPORequest;
import edu.fpt.groupfive.dto.warehouse.request.InboundReturnRequest;
import java.util.List;
import java.util.Map;

public interface WarehouseInboundService {
    void processPOInbound(InboundPORequest request);
    void processReturnInbound(InboundReturnRequest request);
    
    List<Map<String, Object>> getPendingPOs();
    List<Map<String, Object>> getPendingReturns();
}
