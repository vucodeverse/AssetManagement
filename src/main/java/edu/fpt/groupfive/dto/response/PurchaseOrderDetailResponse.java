package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderDetailResponse {
    private Integer orderId;
    private String status;
    private LocalDate createdAt;
    private String supplierName;
    private String orderNote;

    // Totals
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;

    private List<OrderDetailResponse> items;
}
