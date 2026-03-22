package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AssetCapacity;

import java.util.List;
import java.util.Optional;

public interface WhAssetCapacityDAO {
    List<AssetCapacity> findAll();
    Optional<AssetCapacity> findByAssetTypeId(int assetTypeId);
    void saveOrUpdate(int assetTypeId, int unitVolume);
}
