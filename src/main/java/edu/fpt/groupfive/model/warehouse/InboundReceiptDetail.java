package edu.fpt.groupfive.model.warehouse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundReceiptDetail {
    private Integer receiptDetailId;
    private Integer receiptId;
    private Integer purchaseOrderDetailId;
    private Integer assetTypeId;
    private Integer quantityReceived;
}
