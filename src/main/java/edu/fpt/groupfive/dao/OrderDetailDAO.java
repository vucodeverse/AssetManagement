package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.OrderDetail;

import java.sql.Connection;
import java.util.List;

public interface OrderDetailDAO {
    Integer insert(OrderDetail orderDetail, Integer orderId,  Connection connection);
    List<OrderDetail> findByOrderId(Integer orderId);
    List<OrderDetail> findAll();
}
