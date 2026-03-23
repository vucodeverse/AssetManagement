package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.model.Asset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetDAO {

    int insert(Asset asset);

    void update(Asset asset);

    void updateStatus(Integer assetId, AssetStatus status);

    void delete(Integer id);

    List<Integer> findValidAssetIds(List<Integer> assetIds, int departmentId);

    void updateAssetDepartment(List<Integer> assetIds, int newDepartmentId);

    Optional<Asset> findById(Integer id);

    List<Asset> findAll();

    List<Asset> findAllByDepartmentId(Integer departmentId);

    List<Asset> findByReturnRequestId(Integer requestId);

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

    List<Asset> searchAssets(
            AssetSearchCriteria criteria,
            int offset,
            int pageSize
    );

    int countAssets(String keyword, AssetStatus status, LocalDate fromDate, LocalDate toDate
    );

    List<Asset> findExpiringWarranties(int days);



    List<Asset> findByDepartmentId(Integer departmentId);
}