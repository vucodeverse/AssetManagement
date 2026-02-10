package edu.fpt.groupfive.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCreateDetailRequest {
    private Integer purchaseRequestDetailId;
    private Integer quantity;
    private String quotationDetailNote;
    private Integer warrantyMonths;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private String assetTypeName;
    private String specificationRequirement;
}
