package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;

import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.model.Purchase;

import java.util.List;

public interface PurchaseService {
    Integer createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId, Request draft);

    PurchaseResponse findById(Integer id);

    List<PurchaseResponse> findAllPurchases();

    List<PurchaseResponse> searchAndFilter(PurchaseSearchAndFilter purchaseSearchAndFilter);

    void actionsWithPurchase(Integer purchaseId, String action, String reasonReject);

    PurchaseCreateRequest loadDraftForEdit(Integer purchaseId);
}
