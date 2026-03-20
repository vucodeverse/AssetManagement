package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderDAO {
    Integer insert(Order order);

    Map<Integer, Integer> getOrderedQuantityByPurchaseDetailId(List<Integer> purchaseDetailIds);

    List<Object[]> search(PurchaseOrderSearchCriteria criteria);

    Optional<Order> findById(Integer orderId);

    List<Order> findRecent();

    void updateStatus(Integer orderId, edu.fpt.groupfive.common.OrderStatus status);
    List<Object[]> findByStatus(edu.fpt.groupfive.common.OrderStatus status);
}
