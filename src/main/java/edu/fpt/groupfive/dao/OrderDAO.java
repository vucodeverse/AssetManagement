package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderDAO {
    Integer insert(Order order);


    Map<Integer, Integer> getOrderedQtyByPurchaseDetail(List<Integer> purchaseDetailIds);

    List<Object[]> searchAndFilter(PurchaseOrderSearchCriteria criteria);

    Optional<Order> findById(Integer orderId);

    long countAll();

    java.math.BigDecimal sumTotalAmount();

    List<Order> findRecent(int limit);
}
