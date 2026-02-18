package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.model.Purchase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseDAO {
    int insert(Purchase purchase);
    Optional<Purchase> findById(Integer purchaseId);
    Optional<Purchase> findByIdAndApproved(Integer purchaseId, String status);
    List<Purchase> findAll();
    List<Purchase> getPurchaseByFilter(Request status, Priority priority, Integer id, String keyword, LocalDate from, LocalDate to);
}
