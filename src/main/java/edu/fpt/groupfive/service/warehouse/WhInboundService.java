package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundPOReceiveRequestDTO;

public interface WhInboundService {
    void processPOInbound(InboundPOReceiveRequestDTO request, Integer userId);
}
