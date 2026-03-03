package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseOrderFullResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = OrderDetailMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toOrder(PurchaseOrderCreateRequest orderCreateRequest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "purchaseId", source = "purchaseRequestId")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "note", source = "orderNote")
    @Mapping(target = "status", expression = "java(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)")
    PurchaseOrderResponse toPurchaseOrderResponse(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "purchaseId", source = "purchaseRequestId")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "note", source = "orderNote")
    @Mapping(target = "items", source = "orderDetails")
    @Mapping(target = "status", expression = "java(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)")
    PurchaseOrderFullResponse toPurchaseOrderFullResponse(Order order);
}
