package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.PurchaseDetail;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface PurchaseDetailDAO {
    void insert(PurchaseDetail purchaseDetail, Connection conn);

    List<PurchaseDetail> findByPurchaseRequestId(Integer purchaseRequestId);

    void deleteByPurchaseRequestId(Integer purchaseRequestId, Connection conn);
}
