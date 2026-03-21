package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Quotation extends AbstractEntity<Integer>{

    private Status status;
    private String quotationNote;
    private Integer supplierId;
    private Integer purchaseId;
    private BigDecimal totalAmount;
    private String rejectedReason;
    private List<QuotationDetail> quotationDetails = new ArrayList<>();
}
