package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDetailResponse {
    private Integer id;
    private String assetTypeName;
    private Integer quantity;
    private BigDecimal estimatePrice;
    private String specificationRequirement;
    private String purchaseDetailNote;
}
