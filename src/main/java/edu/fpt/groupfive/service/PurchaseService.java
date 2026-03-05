package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;

import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;

import java.util.List;

public interface PurchaseService {
    Integer createPurchaseRequest(PurchaseRequestCreateRequest purchaseCreateRequest, int userId, Request draft);

    PurchaseRequestResponse findById(Integer id);

    List<PurchaseRequestResponse> findAllPurchases();

    List<PurchaseRequestResponse> searchAndFilter(PurchaseRequestSearchCriteria purchaseRequestSearchCriteria);

    void actionsWithPurchase(Integer purchaseId, String action, String reasonReject,Integer userId);

    PurchaseRequestCreateRequest loadDraftForEdit(Integer purchaseId);
}
