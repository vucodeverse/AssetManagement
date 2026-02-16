package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class QuotationResponse {
    private Integer quotationId;
    private Integer purchaseId;
    private QuotationStatus quotationStatus;
    private BigDecimal totalAmount;
    private LocalDate createdAt;
    private String supplierName;
    private List<QuotationDetailResponse> quotationDetails;
}
