package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetType;

import java.util.List;
import java.util.Optional;

public interface AssetTypeDAO {
    String findById(Integer id);
    List<AssetType> findAll();

}
