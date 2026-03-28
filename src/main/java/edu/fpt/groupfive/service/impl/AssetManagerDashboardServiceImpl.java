package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.AssetStatus;
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

                List<Asset> allAssets = assetDAO.findAll();
                long totalAssets = allAssets.size();

                long available = allAssets.stream()
                                .filter(a -> a.getCurrentStatus() == AssetStatus.AVAILABLE).count();
                long assigned = allAssets.stream()
                                .filter(a -> a.getCurrentStatus() == AssetStatus.ASSIGNED).count();
                long maintenance = allAssets.stream()
                                .filter(a -> a.getCurrentStatus() == AssetStatus.UNDER_MAINTENANCE).count();
                long disposed = allAssets.stream()
                                .filter(a -> a.getCurrentStatus() == AssetStatus.DISPOSED).count();

                BigDecimal totalValue = allAssets.stream()
                                .map(Asset::getOriginalCost)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<AllocationRequest> pendingRequests = allocationReqDao.findAllPending();
                long pendingCount = pendingRequests.size();
                List<AllocationRequestResponse> pendingResponses = pendingRequests.stream()
                                .limit(5)
                                .map(this::toAllocationResponse)
                                .collect(Collectors.toList());

                List<Asset> expiringAssets = assetDAO.findExpiringWarranties(30);
                long expiringCount = expiringAssets.size();
                List<ExpiringWarrantyAssetResponse> expiringList = expiringAssets.stream()
                                .limit(5)
                                .map(this::toExpiringAsset)
                                .collect(Collectors.toList());

                List<AssetResponse> recentAssets = allAssets.stream()
                                .filter(a -> a.getAcquisitionDate() != null)
                                .sorted(Comparator.comparing(Asset::getAcquisitionDate).thenComparing(Asset::getAssetId, Comparator.reverseOrder()))
                                .limit(5)
                                .map(this::toAssetResponse).toList();

                List<AllocationRequest> allRequests = allocationReqDao.findAllOrderByCreatedAtDesc();

                return AssetManagerDashboardResponse.builder()
                                .totalAssets(totalAssets)
                                .totalAssetValue(totalValue)
                                .availableCount(available)
                                .assignedCount(assigned)
                                .maintenanceCount(maintenance)
                                .disposedCount(disposed)
                                .pendingAllocationRequests(pendingCount)
                                .expiringWarrantyCount(expiringCount)
                                .pendingAllocations(pendingResponses)
                                .expiringWarranties(expiringList)
                                .recentAssets(recentAssets)

                                .build();
        }

        private AllocationRequestResponse toAllocationResponse(AllocationRequest req) {
                String requesterName = userDAO.findFullNameByUserId(req.getRequesterId());
                return AllocationRequestResponse.builder()
                                .requestId(req.getRequestId())
                                .status(req.getStatus())
                                .userName(requesterName)
                                .neededByDate(req.getNeededByDate())
                                .createdAt(req.getCreatedAt())
                                .priority(req.getPriority())
                                .build();
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
                                .currentStatus(asset.getCurrentStatus())
                                .originalCost(asset.getOriginalCost())
                                .assetTypeName(asset.getAssetTypeName())
                                .acquisitionDate(asset.getAcquisitionDate())
                                .build();
        }
}