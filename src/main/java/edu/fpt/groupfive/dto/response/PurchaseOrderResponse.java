package edu.fpt.groupfive.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponse {
    protected Integer orderId;
    protected Integer purchaseId;
    protected LocalDate createdAt;
    protected String supplierName;
    protected BigDecimal totalAmount;
    protected String note;
    protected String status;
    protected String approvedByName;
}
