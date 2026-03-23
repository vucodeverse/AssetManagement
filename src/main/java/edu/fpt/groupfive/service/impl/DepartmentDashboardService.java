package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.response.*;
import edu.fpt.groupfive.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentDashboardService {

    private final AssetDAO assetDAO;
    private final AllocationReqDao allocationReqDao;
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public DepartmentDashboardDTO getDashboardData(Integer userId) {
        Users user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + userId));

        Integer departmentId = user.getDepartmentId();

        Department department = departmentDAO.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy department với id: " + departmentId));

        DepartmentDashboardDTO dashboard = new DepartmentDashboardDTO();

        dashboard.setUserId(user.getUserId());
        dashboard.setUserName(user.getFirstName() + " " + user.getLastName());
        dashboard.setDepartmentId(departmentId);
        dashboard.setDepartmentName(department.getDepartmentName());

        List<Asset> assets = assetDAO.findAllByDepartmentId(departmentId);

        DashboardStatsDTO stats = calculateStats(assets, departmentId);
        dashboard.setTotalAssets(stats.getTotalAssets());
        dashboard.setAssetsInUse(stats.getAssetsInUse());
        dashboard.setPendingAllocations(stats.getPendingAllocations());
        dashboard.setTotalAssetValue(stats.getTotalAssetValue());

        dashboard.setAssetDistribution(getAssetDistribution(assets));

        dashboard.setRecentRequests(getRecentRequests(departmentId));

        return dashboard;
    }


    private DashboardStatsDTO calculateStats(List<Asset> assets, Integer departmentId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalAssets((long) assets.size());

        long inUseCount = assets.stream()
                .filter(a -> a.getCurrentStatus() == AssetStatus.ASSIGNED)
                .count();
        stats.setAssetsInUse(inUseCount);

        BigDecimal totalValue = assets.stream()
                .map(Asset::getOriginalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalAssetValue(totalValue);

        List<AllocationRequest> allRequests = allocationReqDao.findAllByDepartmentId(departmentId);
        long pendingCount = allRequests.stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "SUBMITTED".equals(r.getStatus()))
                .count();
        stats.setPendingAllocations(pendingCount);

        return stats;
    }

    private List<AssetDistributionDTO> getAssetDistribution(List<Asset> assets) {
        if (assets.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> typeCountMap = assets.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAssetTypeName() != null ? a.getAssetTypeName() : "Khác",
                        Collectors.counting()
                ));

        long total = assets.size();

        return typeCountMap.entrySet().stream()
                .map(entry -> {
                    AssetDistributionDTO dto = new AssetDistributionDTO();
                    dto.setTypeName(entry.getKey());
                    dto.setCount(entry.getValue());
                    double percentage = (entry.getValue() * 100.0) / total;
                    dto.setPercentage(Math.round(percentage * 10) / 10.0);
                    return dto;
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }


    private List<RecentRequestDTO> getRecentRequests(Integer departmentId) {
        List<AllocationRequest> requests = allocationReqDao.findAllByDepartmentId(departmentId);

        return requests.stream()
                .sorted((r1, r2) -> {
                    if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                    if (r1.getCreatedAt() == null) return 1;
                    if (r2.getCreatedAt() == null) return -1;
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .limit(5)
                .map(this::mapToRecentRequestDTO)
                .collect(Collectors.toList());
    }


    private RecentRequestDTO mapToRecentRequestDTO(AllocationRequest request) {
        RecentRequestDTO dto = new RecentRequestDTO();
        dto.setRequestId(request.getRequestId());
        dto.setCode("REQ-" + request.getRequestId());

        // Lấy ngày tạo
        if (request.getRequestDate() != null) {
            dto.setDate(request.getRequestDate().format(DATE_FORMATTER));
        } else if (request.getCreatedAt() != null) {
            dto.setDate(request.getCreatedAt().toLocalDate().format(DATE_FORMATTER));
        } else {
            dto.setDate("");
        }

        dto.setReason(truncateString(request.getRequestReason(), 30));
        dto.setStatusText(getStatusText(request.getStatus()));
        dto.setStatusClass(getStatusClass(request.getStatus()));

        return dto;
    }


    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status.toUpperCase()) {
            case "PENDING":
            case "SUBMITTED":
                return "Đang xử lý";
            case "APPROVED":
                return "Đã duyệt";
            case "REJECTED":
                return "Từ chối";
            default:
                return status;
        }
    }


    private String getStatusClass(String status) {
        if (status == null) return "secondary";
        switch (status.toUpperCase()) {
            case "PENDING":
            case "SUBMITTED":
                return "warning";
            case "APPROVED":
                return "success";
            case "REJECTED":
                return "danger";
            default:
                return "secondary";
        }
    }


    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }


    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardStatsDTO {
        private Long totalAssets;
        private Long assetsInUse;
        private Long pendingAllocations;
        private BigDecimal totalAssetValue;
    }
}