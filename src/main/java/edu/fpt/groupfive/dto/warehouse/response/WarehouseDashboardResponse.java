package edu.fpt.groupfive.dto.warehouse.response;

import lombok.Data;

@Data
public class WarehouseDashboardResponse {
    private long pendingPO;
    private long pendingReturn;
    private long pendingAllocation;
    // Map of zone usage for heatmap
}
