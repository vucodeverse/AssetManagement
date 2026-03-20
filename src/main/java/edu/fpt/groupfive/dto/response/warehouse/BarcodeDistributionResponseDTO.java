package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class BarcodeDistributionResponseDTO {
    private Integer purchaseOrderId;
    private Integer assetTypeId;
    private String assetTypeName;
    private Integer quantity;
    private List<BarcodeItemDTO> items;

    @Getter
    @Builder
    public static class BarcodeItemDTO {
        private String assetCode;
        private String serialNumber; // Optional, can be empty for barcode labeling
        private String status;
    }
}
