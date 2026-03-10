package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.WarehouseRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseResponseDTO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarehouseMapper {

    Warehouse toEntity(WarehouseRequestDto dto);

    WarehouseResponseDTO toResp(Warehouse entity);

    List<WarehouseResponseDTO> toListResp(List<Warehouse> all);
}
