package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseRequestDetailCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseRequestDetailResponse;
import edu.fpt.groupfive.model.PurchaseDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseDetailMapper {

    PurchaseDetail toPurchaseDetail(PurchaseRequestDetailCreateRequest purchaseRequestDetailCreateRequest);

    List<PurchaseDetail> toPurchaseDetailList(
            List<PurchaseRequestDetailCreateRequest> purchaseRequestDetailCreateRequests);

    PurchaseRequestDetailResponse toPurchaseDetailResponse(PurchaseDetail purchaseDetail);

    List<PurchaseRequestDetailResponse> toPurchaseDetailResponseList(List<PurchaseDetail> purchaseDetails);

}
