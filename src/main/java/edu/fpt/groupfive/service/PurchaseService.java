package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;

import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;

import java.util.List;

public interface PurchaseService {
    Integer createPurchaseRequest(PurchaseRequestCreateRequest purchaseCreateRequest, int userId, PurchaseProcessStatus draft);

    PurchaseRequestResponse getPurchaseRequestById(Integer id);

    List<PurchaseRequestResponse> getPurchaseRequests();

    List<PurchaseRequestResponse> searchPurchaseRequests(PurchaseRequestSearchCriteria purchaseRequestSearchCriteria);

    void processPurchaseRequestAction(Integer purchaseId, String action, String reasonReject, Integer userId);

    PurchaseRequestCreateRequest preparePurchaseRequestForm(Integer purchaseId);

    int getTotalRequest();

}
