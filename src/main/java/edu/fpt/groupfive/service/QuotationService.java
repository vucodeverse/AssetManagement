package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.response.QuotationSummaryResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationService {
    Integer createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action);

    QuotationCreateRequest checkFormQuotation(Integer purchaseId);

    List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId);

    QuotationResponse getQuotationById(Integer quotationId);

    QuotationCreateRequest getQuotationRequestById(Integer id);

    void actionWithQuota(Integer quotationId, String action,String reason);


    List<QuotationSummaryResponse> searchAndFilterForQuotation(QuotationSearchCriteria quotationSearchCriteria);

    List<QuotationSummaryResponse> getQuotationAndPurchase();

    List<QuotationResponse> quotationCriteriaForPurchase(QuotationSearchCriteria quotationSearchCriteria);

    List<QuotationDetailCreateRequest> mapPurchaseToQuotation(Integer purchaseId);
}
