package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.model.AssetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetTypeMapper {

    AssetTypeResponse toAssetTypeResponse(AssetType assetType);

    AssetType toAssetType(AssetTypeCreateRequest request);

    @Mapping(target = "typeId", ignore = true)
    void updateFromRequest(AssetTypeUpdateRequest request,
                           @MappingTarget AssetType assetType);

    List<AssetTypeResponse> toAssetTypeResponseList(List<AssetType> assetTypes);
}
