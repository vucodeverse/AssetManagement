package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;

public interface PurchaseService {
    void createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId);
}
