package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface OrderService {
    PurchaseOrderCreateRequest checkFormCreateOrder(Integer quotationId);

    Integer createOrder(Integer quotationId, PurchaseOrderCreateRequest orderCreateRequest, String username);

    List<PurchaseOrderResponse> getPurchaseOrders(PurchaseOrderSearchCriteria criteria);

    PurchaseOrderResponse getOrderDetail(Integer orderId);

    void updateDeliveryDate(Integer orderId, String deliveryDate);
}
