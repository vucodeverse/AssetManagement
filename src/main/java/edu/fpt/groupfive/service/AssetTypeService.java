package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.model.AssetType;

import java.util.List;

public interface AssetTypeService {
    List<AssetTypeResponse> getAllAssetType();
}
