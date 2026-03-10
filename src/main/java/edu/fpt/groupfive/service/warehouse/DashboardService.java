package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.response.DashboardMetricsDto;

public interface DashboardService {
    DashboardMetricsDto getDashboardData(Integer managerUserId);
}
