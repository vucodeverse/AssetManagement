package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.dto.response.QuotationForPurchaseResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationService {
    void createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action);

    QuotationCreateRequest checkFormQuotation(Integer purchaseId);

    List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId);

    QuotationResponse getQuotationById(Integer quotationId);

    QuotationCreateRequest getQuotationRequestById(Integer id);

    void rejectQuotation(Integer quotationId, String reason);

    List<QuotationForPurchaseResponse> searchAndFilterForQuotation(SearchForQuotation searchForQuotation);

    List<QuotationForPurchaseResponse> getQuotationAndPurchase();

    List<QuotationResponse> quotationCriteriaForPurchase(QuotationSearchCriteria quotationSearchCriteria);

    List<QuotationCreateDetailRequest> mapPurchaseToQuotation(Integer purchaseId);
}
