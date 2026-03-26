package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import java.util.List;
import java.util.Map;

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

    /**
     * Lấy chi tiết một lệnh bàn giao/cấp phát.
     *
     * @param handoverId ID của lệnh handover
     * @return HandoverDetailResponseDTO
     */
    HandoverDetailResponseDTO getHandoverDetail(Integer handoverId);

    /**
     * Xác nhận xuất kho cho lệnh bàn giao.
     * 
     * @param handoverId ID lệnh bàn giao
     * @param assets Map chứa assetId và zoneId của các tài sản được chọn
     * @param username Tên đăng nhập người thực hiện
     * @param note Ghi chú phiếu xuất
     */
    void confirmOutbound(Integer handoverId, Map<Integer, Integer> assets, String username, String note);
}
