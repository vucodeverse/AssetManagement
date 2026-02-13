package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = OrderDetailMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    Order toOrder(OrderCreateRequest orderCreateRequest);
}
