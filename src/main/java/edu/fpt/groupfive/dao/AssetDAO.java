package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.model.Asset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetDAO {

    void insert(Asset asset);

    void update(Asset asset);

    void delete(Integer id);

    Optional<Asset> findById(Integer id);

    List<Asset> findAll();

    List<Asset> findAllByDepartmentId(Integer departmentId);

    Optional<AssetDetailResponse> findDetailById(Integer id);
    List<Asset> searchAssets(
            String keyword,
            AssetStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            String direction,
            int offset,
            int pageSize
    );

    int countAssets(String keyword, AssetStatus status, LocalDate fromDate, LocalDate toDate
    );


}