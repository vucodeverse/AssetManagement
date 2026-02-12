package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.model.QuotationDetail;

import java.util.List;
import java.util.Optional;

public interface QuotationDetailDAO {

    Integer insert(QuotationDetail quotationDetail);
    Optional<QuotationDetail> findById(Integer quotationDetailId);

    List<QuotationDetail> findByPurchaseId(Integer purchaseId);

    List<QuotationDetail> findByQuotationId(Integer quotationId);
}
