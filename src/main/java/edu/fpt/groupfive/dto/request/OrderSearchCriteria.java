package edu.fpt.groupfive.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
public class OrderSearchCriteria {
    private String keyword;
    private String status;
    private String supplierName;
    private String amountRange;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

}
