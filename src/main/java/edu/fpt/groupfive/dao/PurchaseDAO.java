package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Request;
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

    List<Purchase> getPurchaseByFilter(PurchaseRequestSearchCriteria purchaseRequestSearchCriteria);

    void updatePurchaseStatus(Request request, Integer purchaseId, String reasonReject, Integer userId);

    void update(Purchase purchase);

    Map<Integer, Object[]> findQuotaSummaryByFilter(QuotationSearchCriteria s);

}
