package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.InboundPORequest;
import edu.fpt.groupfive.dto.warehouse.request.InboundReturnRequest;

public interface WarehouseInboundService {
    void processPOInbound(InboundPORequest request);
    void processReturnInbound(InboundReturnRequest request);
}
