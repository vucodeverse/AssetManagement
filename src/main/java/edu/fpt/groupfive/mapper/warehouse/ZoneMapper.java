package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.ZoneRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponseDto;
import edu.fpt.groupfive.model.warehouse.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZoneMapper {

    Zone toEntity(ZoneRequestDto dto);

    ZoneResponseDto toResp(Zone entity);

    List<ZoneResponseDto> toListResp(List<Zone> zones);
}
