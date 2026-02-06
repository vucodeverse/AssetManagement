package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseDetailCreateRequest;
import edu.fpt.groupfive.model.PurchaseDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseDetailMapper {

    PurchaseDetail toPurchaseDetail(PurchaseDetailCreateRequest purchaseDetailCreateRequest);

    List<PurchaseDetail> toPurchaseDetailList(List<PurchaseDetailCreateRequest> purchaseDetailCreateRequests);
}
