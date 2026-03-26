package edu.fpt.groupfive.dto.response.warehouse;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundReceiptResponseDTO {
    private Integer receiptId;
    private Integer purchaseOrderId;
    private String supplierName;
    private String deliveryNote;
    private String receivedBy;
    private LocalDateTime receivedAt;
    private Integer totalAssets;
}
