package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.model.Asset;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper {

    Asset toAsset(AssetCreateRequest request);

    AssetResponse toResponse(Asset asset);

    List<AssetResponse> toResponseList(List<Asset> assets);

    @Mapping(target = "assetId", ignore = true)
    void updateFromRequest(AssetUpdateRequest request,
                           @MappingTarget Asset asset);
}