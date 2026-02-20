package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.model.PurchaseDetail;

import java.util.List;
import java.util.Optional;

public interface PurchaseDetailDAO {
    void insert(PurchaseDetail purchaseDetail);
    Optional<PurchaseDetail> findById(Integer purchaseDetailId);
    List<PurchaseDetail> findByPurchaseRequestId(Integer purchaseRequestId);
}

