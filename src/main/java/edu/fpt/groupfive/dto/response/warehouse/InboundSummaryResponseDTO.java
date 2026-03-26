package edu.fpt.groupfive.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundSummaryResponseDTO {
    private Integer purchaseOrderId;
    private Integer receiptId;
    private String receiptNo;
    private LocalDateTime inboundDate;
    private List<AssetGroupDTO> assetGroups;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetGroupDTO {
        private String assetTypeName;
        private Integer quantity;
        private List<AssetDetailDTO> assets;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetDetailDTO {
        private Integer assetId;
        private String assetName;
        private String status;
        private Double cost;
    }
}
