package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuotationDetailMapper {

    @Mapping(target = "purchaseDetailId", source = "purchaseRequestDetailId")
    QuotationDetail toQuotationDetail(QuotationDetailCreateRequest quotationDetailCreateRequest);

    @Mapping(target = "quotationDetailId", source = "id")
    QuotationDetailResponse toQuotationDetailResponse(QuotationDetail quotationDetail);
}
