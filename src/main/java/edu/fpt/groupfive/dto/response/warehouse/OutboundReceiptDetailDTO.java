package edu.fpt.groupfive.dto.response.warehouse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chi tiết phiếu xuất kho - dùng cho màn hình xem chi tiết phiếu xuất kho.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundReceiptDetailDTO {

    // --- Thông tin phiếu ---
    private Integer receiptId;
    private String receiptNo;
    private String receiptType;
    private LocalDateTime createdAt;
    private String creatorName;
    private String note;

    // --- Liên kết ---
    private Integer assetHandoverId;
    private String handoverStatus;     // PENDING / COMPLETED
    private String toDepartmentName;   // Phòng ban nhận

    // --- Nội dung ---
    private List<AssetGroupDTO> assetGroups;
    private Integer totalQuantity;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetGroupDTO {
        private String assetTypeName;
        private Integer quantity;
        private List<AssetItemDTO> assets;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetItemDTO {
        private Integer assetId;
        private String assetName;
        private String status;
    }
}
