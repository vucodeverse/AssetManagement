package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.model.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = PurchaseDetailMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PurchaseMapper {

    @Mapping(source = "purchaseDetailCreateRequests", target = "purchaseDetails")
    Purchase toPurchase(PurchaseCreateRequest purchaseCreateRequest);
}
