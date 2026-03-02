package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Priority;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchForQuotation {
    private Priority priority;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private String amountRange;
    private String keyword;
    private Integer purchaseId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
}
