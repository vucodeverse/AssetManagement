package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.PageResponse;

import java.util.List;

public interface AssetService {
    PageResponse<AssetDetailResponse> searchAssets(
            AssetSearchCriteria criteria,
            int page,
            int pageSize
    );

    List<AssetDetailResponse> findAll();

    List<AssetDetailResponse> findByDepartment(Integer departmentId);
}
