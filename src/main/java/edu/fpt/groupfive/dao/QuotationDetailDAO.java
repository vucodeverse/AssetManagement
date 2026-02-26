package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;

import java.util.List;
import java.util.Optional;

public interface QuotationDetailDAO {

    Integer insert(QuotationDetail quotationDetail);

    Optional<QuotationDetail> findById(Integer quotationDetailId);

    List<QuotationDetail> findByPurchaseId(Integer purchaseId);

    List<QuotationDetail> findByQuotationId(Integer quotationId);

    void deleteByQuotationId(Integer quotationId);

    List<QuotationDetailResponse> findDetailByQuotationId(Integer quotationId);
}
