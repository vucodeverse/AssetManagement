package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponse {
    private Integer orderId;
    private Integer purchaseId;
    private LocalDate createdAt;
    private String supplierName;
    private BigDecimal totalAmount;
    private String note;
    private String status;
    private String approvedByName;
}
