package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Quotation extends AbstractEntity<Integer>{

    private QuotationStatus quotationStatus;
    private String quotationNote;
    private Integer supplierId;
    private Integer purchaseId;
    private BigDecimal totalAmount;
    private List<QuotationDetail> quotationDetails = new ArrayList<>();
}
