package edu.fpt.groupfive.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponse {
    protected Integer orderId;
    protected Integer purchaseId;
    protected LocalDateTime createdAt;
    protected String supplierName;
    protected BigDecimal totalAmount;
    protected String orderNote;
    protected String orderStatus;
    protected String approvedByName;

    private List<PurchaseOrderDetailResponse> orderDetails;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
}
