package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = OrderDetailMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toOrder(PurchaseOrderCreateRequest orderCreateRequest);

    @Mapping(target = "orderId", source = "id")
    PurchaseOrderResponse toPurchaseOrderResponse(Order order);
}
