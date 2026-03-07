package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardDTO {
    
    private List<PurchaseRequestResponse> recentPRs;
    private List<QuotationResponse> recentQuotations;
}
