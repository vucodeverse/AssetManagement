package edu.fpt.groupfive.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QuotationCreateDetailRequest {
    private Integer quantity;
    private String note;
    private LocalDate warrantyMonths;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
}
