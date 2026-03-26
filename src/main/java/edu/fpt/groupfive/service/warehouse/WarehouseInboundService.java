package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundReceiptResponseDTO;
import edu.fpt.groupfive.model.warehouse.InboundReceipt;
import java.util.List;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username);
    List<InboundReceipt> getReceiptsByOrderId(Integer orderId);
    InboundSummaryResponseDTO getInboundReceiptSummary(Integer receiptId);
    List<InboundReceiptResponseDTO> getAllInboundReceipts();
}
