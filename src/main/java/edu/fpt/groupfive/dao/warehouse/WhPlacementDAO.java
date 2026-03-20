package edu.fpt.groupfive.dao.warehouse;

import java.sql.Connection;

public interface WhPlacementDAO {
    void placeAsset(Integer assetId, Integer zoneId, Integer userId);
    void placeAsset(Integer assetId, Integer zoneId, Integer userId, Connection conn);
}
