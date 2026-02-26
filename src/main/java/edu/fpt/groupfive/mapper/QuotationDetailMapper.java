package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuotationDetailMapper {
    QuotationDetail toQuotationDetail(QuotationCreateDetailRequest quotationCreateDetailRequest);

    QuotationDetailResponse toQuotationDetailResponse(QuotationDetail quotationDetail);
}
