package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Asset;

import java.util.List;
import java.util.Optional;

public interface AssetDAO {

    void insert(Asset asset);

    void update(Asset asset);

    void delete(Integer id);

    Optional<Asset> findById(Integer id);

    List<Asset> findAll();

    boolean existsBySerial(String serialNumber);
}