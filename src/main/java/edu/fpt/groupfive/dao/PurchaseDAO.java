package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Purchase;

import java.util.Optional;

public interface PurchaseDAO {
    int insert(Purchase purchase);
    Optional<Purchase> findById(Integer purchaseId);
    Optional<Purchase> findByIdAndApproved(Integer purchaseId, String status);
}
