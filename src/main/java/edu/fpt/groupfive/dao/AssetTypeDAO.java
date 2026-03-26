package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
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

    List<AssetType> search(String keyword,
                           Integer categoryId,
                           AssetTypeClass typeClass,
                           DepreciationMethod depreciationMethod,
                           String direction,
                           int offset,
                           int limit);

    int count(String keyword,
              Integer categoryId,
              AssetTypeClass typeClass,
              DepreciationMethod depreciationMethod);
}