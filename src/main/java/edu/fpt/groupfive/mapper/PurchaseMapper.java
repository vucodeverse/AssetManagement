package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;
import edu.fpt.groupfive.model.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = PurchaseDetailMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseMapper {

    @Mapping(source = "purchaseRequestDetailCreateRequests", target = "purchaseDetails")
    Purchase toPurchase(PurchaseRequestCreateRequest purchaseRequestCreateRequest);

    @Mapping(source = "id", target = "purchaseId")
    PurchaseRequestResponse toPurchaseResponse(Purchase purchase);

}
