package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.OrderDetail;
import java.util.List;

public interface OrderDetailDAO {
    Integer insetOrderDetail(OrderDetail orderDetail, Integer orderId);
    List<OrderDetail> findByOrderId(Integer orderId);
}
