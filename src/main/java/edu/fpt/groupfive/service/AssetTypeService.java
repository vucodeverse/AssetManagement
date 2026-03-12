package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;

import java.util.List;

public interface AssetTypeService {

    List<AssetTypeResponse> getAll();

    AssetTypeResponse getById(Integer id);

    void create(AssetTypeCreateRequest request);

    void update(AssetTypeUpdateRequest request);

    void delete(Integer id);
}
