package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.response.AssetDetailResponse;
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
     * Kiểm tra tài sản có hợp lệ để xuất cho lệnh bàn giao này hay không.
     *
     * @param assetCode   Mã tài sản
     * @param handoverId  ID lệnh bàn giao
     * @param stagedCodes Danh sách mã tài sản đã chọn tạm thời
     * @return Thông tin tài sản nếu hợp lệ
     */
    AssetDetailResponse validateAssetForOutbound(String assetCode, Integer handoverId, List<String> stagedCodes);

    /**
     * Lấy thông tin danh sách tài sản theo mã.
     *
     * @param assetCodes Danh sách mã tài sản
     * @return Danh sách AssetDetailResponse
     */
    List<AssetDetailResponse> getAssetsByCodes(List<String> assetCodes);

    /**
     * Xác nhận xuất kho hàng loạt và tạo phiếu xuất.
     *
     * @param handoverId ID lệnh bàn giao
     * @param assetCodes Danh sách mã tài sản đã chọn
     * @param executedBy ID người thực hiện
     */
    void confirmOutbound(Integer handoverId, List<String> assetCodes, Integer executedBy);

    /**
     * @deprecated Sử dụng validateAssetForOutbound và confirmOutbound thay thế.
     */
    @Deprecated
    boolean processScan(Integer handoverId, String assetCode, Integer executedBy);
}
