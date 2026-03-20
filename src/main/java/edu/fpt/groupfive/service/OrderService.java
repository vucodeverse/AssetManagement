package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface OrderService {
    PurchaseOrderCreateRequest preparePurchaseOrderForm(Integer quotationId);

    Integer createPurchaseOrder(Integer quotationId, PurchaseOrderCreateRequest orderCreateRequest, String username);

    List<PurchaseOrderResponse> searchPurchaseOrders(PurchaseOrderSearchCriteria criteria);

    PurchaseOrderResponse getPurchaseOrderById(Integer orderId);

    void updateDeliveryDate(Integer orderId, String deliveryDate);
    List<PurchaseOrderDetailResponse> getAllOrderDetails();
}
