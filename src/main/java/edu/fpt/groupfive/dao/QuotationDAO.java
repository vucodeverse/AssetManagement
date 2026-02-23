package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Quotation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface QuotationDAO {
    Integer insert(Quotation quotation);
    Optional<Quotation> findById(Integer quotationId);
    List<Quotation> findByPurchaseId(Integer purchaseId);

    List<Quotation> getAll();
    BigDecimal totalAmoutForPurchaseId(Integer purchaseId);

    Integer countQuotationFromPurchaseId(Integer purchaseId);

    List<Quotation> searchAndFilterQuotationOfPurchase(QuotationSearchCriteria criteria);
}
