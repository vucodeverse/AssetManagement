package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuotationSearchCriteria {
    private String keyword;
    private QuotationStatus status;
    private String amountRange;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String supplierName;
}
