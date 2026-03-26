package edu.fpt.groupfive.dto.request.warehouse;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundRequestDTO {
    private Integer purchaseOrderId;
    private String deliveryNote;
    private String note;
    private List<InboundItemRequestDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InboundItemRequestDTO {
        private Integer purchaseOrderDetailId;
        private Integer assetTypeId;
        private Integer quantity;
    }
}
