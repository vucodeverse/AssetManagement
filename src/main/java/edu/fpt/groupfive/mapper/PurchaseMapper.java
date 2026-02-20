package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.model.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = PurchaseDetailMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseMapper {
    
    @Mapping(source = "purchaseDetailCreateRequests", target = "purchaseDetails")
    Purchase toPurchase(PurchaseCreateRequest purchaseCreateRequest);

    @Mapping(source = "purchaseDetails", target = "purchaseDetails")
    PurchaseResponse toPurchaseResponse(Purchase purchase);
}
