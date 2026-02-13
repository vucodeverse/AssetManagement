package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Order;

import java.util.List;
import java.util.Map;

public interface OrderDAO {
    Integer insert(Order order);
    Map<Integer, Integer> getOrderedQtyByQuotationDetail(List<Integer> quotationDetailId);
}
