package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.AssetCapacityRequest;
import edu.fpt.groupfive.dto.warehouse.AssetCapacityResponse;

public interface AssetCapacityService {
    AssetCapacityResponse createOrUpdateCapacity(AssetCapacityRequest request);

    AssetCapacityResponse getCapacityByAssetTypeId(Integer assetTypeId);
}
