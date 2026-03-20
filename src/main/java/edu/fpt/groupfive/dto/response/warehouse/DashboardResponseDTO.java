package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponseDTO {
    private int pendingInboundPO;
    private int pendingInboundReturn;
    private int pendingOutboundAllocation;
    private List<ZoneCapacityResponseDTO> capacityHeatmap;
    private List<LedgerRecordResponseDTO> recentActivities;
}
