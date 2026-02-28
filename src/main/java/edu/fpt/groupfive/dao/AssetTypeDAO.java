package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetType;

import java.util.List;
import java.util.Optional;

public interface AssetTypeDAO {
    List<AssetType> findAll();

    AssetType findById(Integer id);

    void insert(AssetType assetType);

    void update(AssetType assetType);

    void delete(Integer id);

    boolean existAssetUsingType(Integer typeId);

    boolean existByTypeName(String typeName);

}
