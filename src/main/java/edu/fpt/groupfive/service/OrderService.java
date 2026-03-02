package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderFullResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface OrderService {
    PurchaseOrderCreateRequest checkFormCreateOrder(Integer quotationId);

    Integer createOrder(Integer quotationId, PurchaseOrderCreateRequest orderCreateRequest);

    List<PurchaseOrderResponse> getPurchaseOrdersFlat(PurchaseOrderSearchCriteria criteria);

    PurchaseOrderFullResponse getOrderDetail(Integer orderId);
}
