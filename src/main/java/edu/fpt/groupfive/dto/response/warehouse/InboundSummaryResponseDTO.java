package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class InboundSummaryResponseDTO {
    private Integer receiptId;
    private Integer purchaseOrderId;
    private String supplierName;
    private LocalDateTime inboundDate;
    private List<AssetGroupDTO> assetGroups;

    @Getter
    @Setter
    @Builder
    public static class AssetGroupDTO {
        private String assetTypeName;
        private Integer quantity;
        private List<Integer> assetIds;
    }
}
