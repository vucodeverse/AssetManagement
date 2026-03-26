package edu.fpt.groupfive.model.warehouse;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhReceipt {
    private Integer receiptId;
    private String receiptNo;
    private Integer purchaseOrderId;
    private Integer assetHandoverId;
    private String receiptType; // INBOUND_PO, INBOUND_RETURN
    private LocalDateTime createdAt;
    private Integer createdBy;
    private String creatorName; // To display name instead of ID
    private Integer totalQuantity; // Count of assets in this receipt
    private String note;
}
