package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InventoryVoucherResponseDTO;
import edu.fpt.groupfive.model.warehouse.InventoryVoucher;
import java.util.List;

public interface WarehouseInboundService {
    InboundSummaryResponseDTO processInboundPO(InboundRequestDTO request, String username);
    List<InventoryVoucher> getVouchersByOrderId(Integer orderId);
    InboundSummaryResponseDTO getInboundVoucherSummary(Integer voucherId);
    List<InventoryVoucherResponseDTO> getAllInboundVouchers();
}
