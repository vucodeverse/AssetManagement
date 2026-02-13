package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;

public interface OrderService {
    OrderCreateRequest checkFormCreateOrder(Integer quotationId);
    void createOrder(Integer quotationId, OrderCreateRequest orderCreateRequest);
}
