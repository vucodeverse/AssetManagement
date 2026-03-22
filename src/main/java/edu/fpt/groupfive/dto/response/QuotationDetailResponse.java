package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDetailResponse {
    private Integer quotationId;
    private Integer quotationDetailId;
    private String assetTypeName;
    private Integer quantity;
    private Integer warrantyMonths;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private String quotationDetailNote;
    private String specificationRequirement;
    private Integer purchaseDetailId;
    private Integer assetTypeId;
    private PurchaseProcessStatus status;
}
