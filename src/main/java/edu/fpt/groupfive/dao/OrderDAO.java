package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderDAO {
    Integer insert(Order order);

    Map<Integer, Integer> getOrderedQtyByQuotationDetail(List<Integer> quotationDetailId);

    Map<Integer, Integer> getOrderedQtyByPurchaseDetail(List<Integer> purchaseDetailIds);

    List<Object[]> searchAndFilter(OrderSearchCriteria criteria);

    Optional<Order> findById(Integer orderId);

    long countAll();

    java.math.BigDecimal sumTotalAmount();

    List<Order> findRecent(int limit);
}
