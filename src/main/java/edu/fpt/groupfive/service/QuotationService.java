package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.dto.response.QuotationForPurchaseResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationService {
    void createQuotation(Integer purchaseId,QuotationCreateRequest quotationCreateRequest);
    QuotationCreateRequest checkFormQuotation(Integer purchaseId);
    List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId);

    QuotationResponse getQuotationById(Integer quotationId);
    List<QuotationForPurchaseResponse> searchAndFilterForQuotation(SearchForQuotation searchForQuotation);

    List<QuotationForPurchaseResponse> getQuotationAndPurchase();


}
