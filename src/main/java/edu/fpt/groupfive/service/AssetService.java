package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetResponse;

import java.util.List;

public interface AssetService {

    List<AssetResponse> getAll();

    AssetResponse getById(Integer id);

    void create(AssetCreateRequest request);

    void update(Integer id, AssetUpdateRequest request);

    void delete(Integer id);
}