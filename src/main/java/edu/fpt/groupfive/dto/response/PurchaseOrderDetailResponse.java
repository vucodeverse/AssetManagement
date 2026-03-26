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
    private Integer purchaseRequestDetailId;
    private Integer assetTypeId;
    private String assetTypeName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private LocalDate deliveryDate;
    private Integer receivedQuantity;
}
