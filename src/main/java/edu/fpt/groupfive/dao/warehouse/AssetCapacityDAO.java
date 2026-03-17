package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import java.util.Optional;

public interface AssetCapacityDAO {
    void upsert(AssetCapacity capacity);
    Optional<AssetCapacity> findByAssetTypeId(Integer assetTypeId);
}
