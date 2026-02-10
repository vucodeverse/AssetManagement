package edu.fpt.groupfive.model;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseDetail extends AbstractEntity<Integer>{
    private Integer quantity;
    private String specificationRequirement;
    private String note;
    private Integer assetTypeId;
    private Integer purchaseRequestId;
    private BigDecimal price;
}
