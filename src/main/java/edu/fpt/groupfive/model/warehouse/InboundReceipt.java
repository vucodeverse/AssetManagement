package edu.fpt.groupfive.model.warehouse;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundReceipt {
    private Integer receiptId;
    private Integer purchaseOrderId;
    private String deliveryNote;
    private Integer receivedBy;
    private LocalDateTime receivedAt;
    private String note;
}
