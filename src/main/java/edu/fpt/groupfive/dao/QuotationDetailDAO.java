package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.model.QuotationDetail;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface QuotationDetailDAO {

    Integer insert(QuotationDetail quotationDetail, Connection connection);

    Optional<QuotationDetail> findById(Integer quotationDetailId);

    List<QuotationDetail> findByQuotationId(Integer quotationId);

    void deleteByQuotationId(Integer quotationId, Connection connection);

    void update(Integer quotationId, Status status);

}