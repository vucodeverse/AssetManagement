package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StaffDashboardDTO {
    private List<PurchaseRequestResponse> approvedPRs;
    private List<QuotationResponse> recentQuotations;
    private List<PurchaseOrderResponse> activeOrders;
}
