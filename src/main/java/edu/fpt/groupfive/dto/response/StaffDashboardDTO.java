package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StaffDashboardDTO {
    private List<PurchaseResponse> approvedPRs;
    private List<QuotationResponse> recentQuotations;
    private List<PurchaseOrderResponse> activeOrders;

    private long awaitingQuoCount;
}
