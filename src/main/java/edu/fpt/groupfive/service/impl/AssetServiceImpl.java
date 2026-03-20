package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetDAO assetDAO;
    private final AssetTypeDAO assetTypeDAO;
    @Override
    public PageResponse<AssetDetailResponse> searchAssets(
            AssetSearchCriteria criteria,
            int pageNo,
            int pageSize
    ) {
        if (criteria == null) criteria = new AssetSearchCriteria();

        if (pageNo < 0) pageNo = 0;
        if (pageSize <= 0) pageSize = 10;

        int offset = pageNo * pageSize;
        List<Asset> assets = assetDAO.searchAssets(criteria, offset, pageSize);

        int totalRecords = assetDAO.countAssets(
                criteria.getKeyword(),
                criteria.getStatus(),
                criteria.getAcquisitionFrom(),
                criteria.getAcquisitionTo()
        );

        List<AssetDetailResponse> responses = assets.stream()
                .map(asset -> AssetDetailResponse.builder()
                        .assetId(asset.getAssetId())
                        .assetName(asset.getAssetName())
                        .assetTypeId(asset.getAssetTypeId())
                        .purchaseOrderDetailId(asset.getPurchaseOrderDetailId())
                        .currentStatus(asset.getCurrentStatus())
                        .originalCost(asset.getOriginalCost())
                        .departmentId(asset.getDepartmentId())
                        .acquisitionDate(asset.getAcquisitionDate())
                        .inServiceDate(asset.getInServiceDate())
                        .warrantyStartDate(asset.getWarrantyStartDate())
                        .warrantyEndDate(asset.getWarrantyEndDate())
                        .assetTypeName(asset.getAssetTypeName())
                        .build())
                .toList();

        return new PageResponse<>(responses, pageNo, pageSize, totalRecords);
    }

    @Override
    public List<AssetDetailResponse> findAll() {

        List<Asset> assets = assetDAO.findAll();
        return assets.stream()
                .map(asset -> {
                    AssetDetailResponse dto = new AssetDetailResponse();

                    dto.setAssetId(asset.getAssetId());
                    dto.setAssetName(asset.getAssetName());
                    dto.setAssetTypeId(asset.getAssetTypeId());
                    dto.setPurchaseOrderDetailId(asset.getPurchaseOrderDetailId());
                    dto.setCurrentStatus(asset.getCurrentStatus());
                    dto.setOriginalCost(asset.getOriginalCost());
                    dto.setDepartmentId(asset.getDepartmentId());

                    dto.setAcquisitionDate(asset.getAcquisitionDate());
                    dto.setInServiceDate(asset.getInServiceDate());
                    dto.setWarrantyStartDate(asset.getWarrantyStartDate());
                    dto.setWarrantyEndDate(asset.getWarrantyEndDate());

                    dto.setAssetTypeName(asset.getAssetTypeName());

                    return dto;
                })
                .toList();
    }
    @Override
    public List<AssetDetailResponse> findByDepartment(Integer departmentId) {

        List<Asset> assets = assetDAO.findByDepartmentId(departmentId);

        return assets.stream()
                .map(asset -> {
                    AssetDetailResponse dto = new AssetDetailResponse();

                    dto.setAssetId(asset.getAssetId());
                    dto.setAssetName(asset.getAssetName());
                    dto.setAssetTypeId(asset.getAssetTypeId());
                    dto.setPurchaseOrderDetailId(asset.getPurchaseOrderDetailId());
                    dto.setCurrentStatus(asset.getCurrentStatus());
                    dto.setOriginalCost(asset.getOriginalCost());
                    dto.setDepartmentId(asset.getDepartmentId());

                    dto.setAcquisitionDate(asset.getAcquisitionDate());
                    dto.setInServiceDate(asset.getInServiceDate());
                    dto.setWarrantyStartDate(asset.getWarrantyStartDate());
                    dto.setWarrantyEndDate(asset.getWarrantyEndDate());

                    dto.setAssetTypeName(asset.getAssetTypeName());

                    return dto;
                })
                .toList();
    }

}