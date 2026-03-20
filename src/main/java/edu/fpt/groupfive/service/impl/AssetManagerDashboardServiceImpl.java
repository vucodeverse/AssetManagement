package edu.fpt.groupfive.service.impl;


import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.response.*;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.service.AssetManagerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetManagerDashboardServiceImpl implements AssetManagerDashboardService {

    private final AssetDAO assetDAO;
    private final AllocationReqDao allocationReqDao;
    private final UserDAO userDAO;

    @Override
    public AssetManagerDashboardResponse getDashboardData() {
        // 1. Lấy tất cả asset
        List<Asset> allAssets = assetDAO.findAll();

        // 2. Tổng số và tổng giá trị
        long totalAssets = allAssets.size();
        BigDecimal totalValue = allAssets.stream()
                .map(Asset::getOriginalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3.yêu cầu cấp phát chờ duyệt
        List<AllocationRequest> pendingRequests = allocationReqDao.findAllPending();
        long pendingCount = pendingRequests.size();
        List<AllocationRequestResponse> pendingResponses = pendingRequests.stream()
                .limit(5)
                .map(this::toAllocationResponse)
                .collect(Collectors.toList());

        // 4. tài sản sắp hết bảo hành (30 ngày)
        List<Asset> expiringAssets = assetDAO.findExpiringWarranties(30);
        long expiringCount = expiringAssets.size();
        List<ExpiringWarrantyAssetResponse> expiringList = expiringAssets.stream()
                .limit(5)
                .map(this::toExpiringAsset)
                .collect(Collectors.toList());

        //  5 tài sản mới nhất (dựa trên acquisition_date)
        List<AssetResponse> recentAssets = allAssets.stream()
                .filter(a -> a.getAcquisitionDate() != null)
                .sorted(Comparator.comparing(Asset::getAcquisitionDate).reversed())
                .limit(5)
                .map(this::toAssetResponse)
                .collect(Collectors.toList());

        //  5 yêu cầu cấp phát gần đây (tất cả)
        List<AllocationRequest> allRequests = allocationReqDao.findAllOrderByCreatedAtDesc();
        List<AllocationRequestResponse> recentAllocs = allRequests.stream()
                .limit(5)
                .map(this::toAllocationResponse)
                .collect(Collectors.toList());

        return AssetManagerDashboardResponse.builder()
                .totalAssets(totalAssets)
                .totalAssetValue(totalValue)
                .pendingAllocationRequests(pendingCount)
                .expiringWarrantyCount(expiringCount)
                .pendingAllocations(pendingResponses)
                .expiringWarranties(expiringList)
                .recentAssets(recentAssets)
                .recentAllocations(recentAllocs)
                .build();
    }

    private AllocationRequestResponse toAllocationResponse(AllocationRequest req) {
        String requesterName = userDAO.findFullNameById(req.getRequesterId());
        return new AllocationRequestResponse(
                req.getRequestId(),
                req.getStatus(),
                requesterName,
                req.getNeededByDate(),
                req.getCreatedAt(),
                req.getPriority()
        );
    }

    private ExpiringWarrantyAssetResponse toExpiringAsset(Asset asset) {
        int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), asset.getWarrantyEndDate());
        return ExpiringWarrantyAssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .warrantyEndDate(asset.getWarrantyEndDate())
                .daysRemaining(days)
                .build();
    }

    private AssetResponse toAssetResponse(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .currentStatus(asset.getCurrentStatus().name())
                .originalCost(asset.getOriginalCost())
                .assetTypeName(asset.getAssetTypeName())
                .acquisitionDate(asset.getAcquisitionDate())
                .build();
    }
}