package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import java.util.List;

/**
 * Service cho các hoạt động xuất kho (Outbound) trong module Kho.
 */
public interface WarehouseOutboundService {
    /**
     * Lấy danh sách các lệnh cấp phát đang chờ (Status.PENDING).
     *
     * @return Danh sách HandoverResponseDTO
     */
    List<HandoverResponseDTO> getPendingAllocations();
}
