package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AssetPlacement;
import java.util.Optional;

public interface AssetPlacementDAO {
    void insert(AssetPlacement placement);
    void delete(Integer assetId);
    Optional<AssetPlacement> findByAssetId(Integer assetId);
}
