package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationForPurchaseResponse {

    private Integer purchaseId;
    private LocalDate needByDate;
    private Integer numberOfQuotation;
    private BigDecimal estPrice;
    private String  priority;
    private LocalDate createdAt;

}

