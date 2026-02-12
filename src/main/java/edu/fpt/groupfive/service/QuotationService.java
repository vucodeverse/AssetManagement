package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationService {
    void createQuotation(Integer purchaseId,QuotationCreateRequest quotationCreateRequest);
    QuotationCreateRequest checkFormQuotation(Integer purchaseId);
    List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId);

    QuotationResponse getQuotationById(Integer quotationId);
}
