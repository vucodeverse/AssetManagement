package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface AssetService {
    PageResponse<AssetDetailResponse> searchAssets(
            AssetSearchCriteria criteria,
            int page,
            int pageSize
    );

    List<AssetResponse> getAll();

    AssetResponse getById(Integer id);

    List<AssetResponse> getAllByDepartmentId (Integer departmentId);

    List<AssetResponse> getAllByReturnRequestId(Integer requestId);

    void create(AssetCreateRequest request);

    void update(Integer id, AssetUpdateRequest request);

    void updateStatus(Integer id, AssetStatus status);

    void delete(Integer id);

    AssetDetailResponse getDetailById(Integer id);

    PageResponse<AssetResponse> searchAssets(
            String keyword,
            AssetStatus status,
            LocalDate formDate,
            LocalDate toDate,            String direction,
            int page
    );
    List<AssetDetailResponse> findAll();

    List<AssetDetailResponse> findByDepartment(Integer departmentId);
}