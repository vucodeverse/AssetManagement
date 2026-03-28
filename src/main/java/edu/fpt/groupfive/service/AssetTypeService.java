package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
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

    List<AssetTypeResponse> search(String keyword,
            Integer categoryId,
            AssetTypeClass typeClass,
            DepreciationMethod depreciationMethod,
            String direction,
            int offset,
            int limit);

    int count(String keyword,
            Integer categoryId,
            AssetTypeClass typeClass,
            DepreciationMethod depreciationMethod);
}
