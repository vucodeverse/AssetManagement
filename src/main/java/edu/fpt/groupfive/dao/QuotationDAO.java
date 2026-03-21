package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Quotation;

import java.util.List;
import java.util.Optional;

public interface QuotationDAO {
    Integer insert(Quotation quotation);

    Optional<Quotation> findById(Integer quotationId);

    void update(Quotation quotation);

    void updateStatus(Integer quotationId, Status status, String rejectedReason);

    List<Quotation> findByPurchaseId(Integer purchaseId);

    Integer countQuotationFromPurchaseId(Integer purchaseId);

    List<Quotation> searchByPurchaseId(QuotationSearchCriteria criteria);

    List<Quotation> findAll();
}
