package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.model.AssetType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssetTypeService {
    List<AssetTypeResponse> getAllAssetType();

    Optional<AssetType> findById(Integer assetTypeId);

    String findNameById(Integer assetTypeId);

    List<AssetTypeResponse> getAll();

    AssetTypeResponse getById(Integer id);

    void create(AssetTypeCreateRequest request);

    void update(AssetTypeUpdateRequest request);

    void delete(Integer id);

    Map<Integer, String> getAssetTypeIdToNameMap();
}
