package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuotationDetail extends AbstractEntity<Integer>{
    private Integer assetTypeId;
    private Integer quantity;
    private String quotationDetailNote;
    private Integer warrantyMonths;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private Integer purchaseDetailId;
    private String rejectedReason;
    private String specificationRequirement;
    private QuotationStatus quotationDetailStatus;
}
