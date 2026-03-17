package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.ZoneRequest;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponse;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    
    @Mapping(target = "zoneId", ignore = true)
    @Mapping(target = "currentCapacity", constant = "0")
    @Mapping(target = "status", constant = "ACTIVE")
    WarehouseZone toEntity(ZoneRequest request);

    @Mapping(target = "usagePercentage", expression = "java(zone.getMaxCapacity() == 0 ? 0 : (double)zone.getCurrentCapacity() / zone.getMaxCapacity() * 100)")
    @Mapping(target = "assetTypeName", ignore = true)
    ZoneResponse toResponse(WarehouseZone zone);
}
