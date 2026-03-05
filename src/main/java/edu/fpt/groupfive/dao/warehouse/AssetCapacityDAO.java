package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AssetCapacity;

public interface AssetCapacityDAO {
    int insert(AssetCapacity capacity);

    int update(AssetCapacity capacity);

    AssetCapacity findByAssetTypeId(Integer assetTypeId);
}
