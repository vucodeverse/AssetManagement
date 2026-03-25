package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationService {
    Integer createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action);

    List<QuotationResponse> getQuotationsByPurchaseId(Integer purchaseId);

    QuotationResponse getQuotationById(Integer quotationId);

    QuotationCreateRequest prepareQuotationUpdateForm(Integer id);

    void processQuotationAction(Integer quotationId, String action);


    List<QuotationResponse> searchQuotationsByPurchaseId(QuotationSearchCriteria quotationSearchCriteria);

    List<QuotationDetailCreateRequest> prepareQuotationForm(Integer purchaseId);

    void processQuotationDetailAction(Integer id, String actions, Integer qoId);
}
