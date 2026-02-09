package edu.fpt.groupfive.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class QuotationDetail extends AbstractEntity<Integer>{
    private AssetType assetType;
    private Quotation quotation;
    private Integer quantity;
    private String quotationDetailNote;
    private Integer warrantyMonths;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private PurchaseDetail purchaseDetail;
}
