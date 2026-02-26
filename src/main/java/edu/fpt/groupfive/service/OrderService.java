package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;

import java.util.List;

public interface OrderService {
    OrderCreateRequest checkFormCreateOrder(Integer quotationId);
    Integer createOrder(Integer quotationId, OrderCreateRequest orderCreateRequest);
    List<PurchaseOrderGroupResponse> getOrdersGroupedByPR(OrderSearchCriteria criteria);
    PurchaseOrderDetailResponse getOrderDetail(Integer orderId);
}


