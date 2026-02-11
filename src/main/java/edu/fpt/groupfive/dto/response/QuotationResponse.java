package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class QuotationResponse {
    private QuotationStatus quotationStatus;
    private BigDecimal totalAmount;
    private LocalDate createdAt;
    private String supllierName;
}
