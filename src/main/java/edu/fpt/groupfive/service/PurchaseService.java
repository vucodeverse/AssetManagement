package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;

import edu.fpt.groupfive.model.Purchase;

public interface PurchaseService {
    void createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId);
    Purchase findById(Integer id);
}
