package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.model.AssetType;

import java.util.List;
import java.util.Optional;

public interface AssetTypeService {
    List<AssetTypeResponse> getAllAssetType();
    Optional<AssetType> findById(Integer assetTypeId);
}
