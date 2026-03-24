package edu.fpt.groupfive.mapper;


import edu.fpt.groupfive.dto.response.AssetLogResponse;
import edu.fpt.groupfive.model.AssetLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetLogMapper {

    AssetLogResponse toResponse(AssetLog assetLog);

    List<AssetLogResponse> toResponseList(List<AssetLog> assetLogs);
}
