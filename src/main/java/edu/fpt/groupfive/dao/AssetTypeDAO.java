package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetType;

import java.util.List;

public interface AssetTypeDAO {
    AssetType findById(Integer id);
    List<AssetType> findAll();
}
