package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.Priority;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationForPurchase {

    private String purchaseId;
    private LocalDate needByDate;
    private Integer quotationOfNumber;
    private BigDecimal estPrice;
    private String  priority;
}
