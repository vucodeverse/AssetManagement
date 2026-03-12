package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Quotation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface QuotationDAO {
    Integer insert(Quotation quotation);

    Optional<Quotation> findById(Integer quotationId);

    void update(Quotation quotation);

    void updateStatusReject(Integer quotationId, QuotationStatus status, String rejectedReason);

    List<Quotation> findByPurchaseId(Integer purchaseId);

    Optional<Quotation> findResponseById(Integer quotationId);

    List<Quotation> findResponsesByPurchaseId(Integer purchaseId);

    List<Quotation> getAll();

    BigDecimal totalAmountForPurchaseId(Integer purchaseId);

    Integer countQuotationFromPurchaseId(Integer purchaseId);

    List<Quotation> searchAndFilterQuotationOfPurchase(QuotationSearchCriteria criteria);

    long countByStatus(edu.fpt.groupfive.common.QuotationStatus status);

    List<Quotation> findRecent(int limit);
}
