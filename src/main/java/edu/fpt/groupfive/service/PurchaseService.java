package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;

import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.model.Purchase;

import java.util.List;

public interface PurchaseService {
    void createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId, Request draft);
    Purchase findById(Integer id);
    List<PurchaseResponse> findAllPurchases();
}
