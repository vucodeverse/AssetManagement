package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.model.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDetailMapper {

        OrderDetail toOrderDetail(PurchaseOrderDetailCreateRequest orderDetailCreateRequest);

        List<OrderDetail> toListOrderDetail(List<PurchaseOrderDetailCreateRequest> orderDetailCreateRequests);

        @Mapping(source = "id", target = "purchaseOrderDetailId")
        PurchaseOrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);

        List<PurchaseOrderDetailResponse> toListOrderDetailResponse(List<OrderDetail> orderDetails);

}
