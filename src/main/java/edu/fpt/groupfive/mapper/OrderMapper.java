package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = OrderDetailMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toOrder(OrderCreateRequest orderCreateRequest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "note", source = "orderNote")
    @Mapping(target = "status", expression = "java(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)")
    PurchaseOrderResponse toPurchaseOrderResponse(Order order);
}
