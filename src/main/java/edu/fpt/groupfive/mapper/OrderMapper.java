package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = OrderDetailMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toOrder(OrderCreateRequest orderCreateRequest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "purchaseId", source = "purchaseRequestId")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "status", expression = "java(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)")
    PurchaseOrderResponse toPurchaseOrderResponse(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "grandTotal", source = "totalAmount")
    @Mapping(target = "status", expression = "java(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)")
    PurchaseOrderDetailResponse toPurchaseOrderDetailResponse(Order order);
}
