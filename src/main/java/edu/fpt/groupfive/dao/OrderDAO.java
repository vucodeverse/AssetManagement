package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.model.PurchaseDetail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderDAO {
    Integer insert(Order order);

    Map<Integer, Integer> getOrderedQuantityByPurchaseDetailId(List<PurchaseDetail> purchaseDetailIds);

    List<Object[]> search(PurchaseOrderSearchCriteria criteria);

    Optional<Order> findById(Integer orderId);

    List<Order> findRecent();

    void updateStatus(Integer orderId, PurchaseProcessStatus status);
}
