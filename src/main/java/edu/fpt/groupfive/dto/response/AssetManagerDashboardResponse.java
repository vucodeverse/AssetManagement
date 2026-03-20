package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AssetManagerDashboardResponse {

    private long totalAssets;
    private BigDecimal totalAssetValue;
    private long pendingAllocationRequests;
    private long expiringWarrantyCount;

    private List<AllocationRequestResponse> pendingAllocations;
    private List<ExpiringWarrantyAssetResponse> expiringWarranties;
    private List<AssetResponse> recentAssets;
    private List<AllocationRequestResponse> recentAllocations;
}
