package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.model.Purchase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseDAO {
    int insert(Purchase purchase);
    Optional<Purchase>  findById(Integer purchaseId);
    Optional<Purchase> findByIdAndApproved(Integer purchaseId, String status);
    List<Purchase> findAll();
    List<Purchase> getPurchaseByFilter(PurchaseSearchAndFilter purchaseSearchAndFilter);

    void updatePurchaseStatus(Request request, Integer purchaseId, String reasonReject);

    List<Purchase> purchaseGropedQuotation(SearchForQuotation s);

    Map<Integer, Object[]> getCountAndTotalAmout(List<Integer> purchaseIds);
}
