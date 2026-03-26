package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import java.util.List;

/**
 * Service cho các hoạt động xuất kho (Outbound) trong module Kho.
 */
public interface WarehouseOutboundService {
    /**
     * Lấy danh sách các lệnh cấp phát (PENDING và COMPLETED).
     *
     * @return Danh sách HandoverResponseDTO
     */
    List<HandoverResponseDTO> getAllocations();

    /**
     * Lấy chi tiết một lệnh bàn giao/cấp phát.
     *
     * @param handoverId ID của lệnh handover
     * @return HandoverDetailResponseDTO
     */
    HandoverDetailResponseDTO getHandoverDetail(Integer handoverId);

    /**
     * Xử lý quét mã tài sản để xuất kho.
     *
     * @param handoverId ID của lệnh bàn giao
     * @param assetCode  Mã tài sản (asset_id)
     * @param executedBy ID người thực hiện
     */
    boolean processScan(Integer handoverId, String assetCode, Integer executedBy);
}
