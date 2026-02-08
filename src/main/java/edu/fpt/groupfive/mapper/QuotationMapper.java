package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.model.Quotation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuotationMapper {

    Quotation toQuotation(QuotationCreateRequest quotationCreateRequest);
}
