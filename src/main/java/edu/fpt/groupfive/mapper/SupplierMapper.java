package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.response.SupplierResponse;
import edu.fpt.groupfive.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {
    SupplierResponse toSupplierResponse(Supplier supplier);
}
