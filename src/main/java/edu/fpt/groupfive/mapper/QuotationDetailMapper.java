package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuotationDetailMapper {
    QuotationDetail toQuotationDetail(QuotationCreateDetailRequest quotationCreateDetailRequest);

    @Mapping(target = "quotationDetailId", source = "id")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "taxRate", source = "taxRate")
    @Mapping(target = "discountRate", source = "discountRate")
    QuotationDetailResponse toQuotationDetailResponse(QuotationDetail quotationDetail);
}
