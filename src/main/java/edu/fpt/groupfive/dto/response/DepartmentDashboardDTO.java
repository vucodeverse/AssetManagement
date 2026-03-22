package edu.fpt.groupfive.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDashboardDTO {

    private Integer userId;
    private String userName;
    private Integer departmentId;
    private String departmentName;
    private Long unreadCount;

    private Long totalAssets;
    private Long assetsInUse;
    private Long pendingAllocations;
    private BigDecimal totalAssetValue;

    private List<AssetDistributionDTO> assetDistribution;

    private List<RecentRequestDTO> recentRequests;


}