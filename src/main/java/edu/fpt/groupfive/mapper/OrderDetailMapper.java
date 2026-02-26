package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.dto.response.OrderDetailResponse;
import edu.fpt.groupfive.model.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDetailMapper
{

     OrderDetail toOrderDetail(OrderDetailCreateRequest orderDetailCreateRequest);

    List<OrderDetail> toListOrderDetail(List<OrderDetailCreateRequest> orderDetailCreateRequests);

    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);
}


