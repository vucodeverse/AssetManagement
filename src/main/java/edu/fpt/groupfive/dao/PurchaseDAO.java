package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Purchase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseDAO {
    int insert(Purchase purchase);

    Optional<Purchase> findById(Integer purchaseId);

    Optional<Purchase> findByIdAndStatus(Integer purchaseId, String status);

    List<Purchase> findAll();

    List<Purchase> search(PurchaseRequestSearchCriteria purchaseRequestSearchCriteria);

    void updateStatus(PurchaseProcessStatus purchaseProcessStatus, Integer purchaseId, String reasonReject, Integer userId);

    void update(Purchase purchase);

    Map<Integer, Object[]> searchQuotationSummary(QuotationSearchCriteria s);

    List<Object[]> getItemOnDB();
}
