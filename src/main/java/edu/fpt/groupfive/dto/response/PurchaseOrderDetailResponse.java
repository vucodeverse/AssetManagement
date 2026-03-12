package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderDetailResponse {
    private Integer purchaseOrderDetailId;
    private String assetTypeName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private LocalDate expectedDeliveryDate;
}
