package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Quotation;

import java.util.List;
import java.util.Optional;

public interface QuotationDAO {
    Integer insert(Quotation quotation);
    Optional<Quotation> findById(Integer quotationId);
    List<Quotation> findByPurchaseId(Integer purchaseId);

    List<Quotation> getAll();
}
