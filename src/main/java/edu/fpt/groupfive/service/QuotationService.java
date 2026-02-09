package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationCreateRequest;

public interface QuotationService {
    void createQuotation(Integer purchaseId,QuotationCreateRequest quotationCreateRequest);
    QuotationCreateRequest checkFormQuotation(Integer purchaseId);
}
