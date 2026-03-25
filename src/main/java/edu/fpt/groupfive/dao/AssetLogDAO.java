package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetLog;

import java.util.List;

public interface AssetLogDAO {

    void insert(AssetLog log);

    List<AssetLog> findByAssetId(int assetId);
}
