package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.response.WarehouseDashboardDto;

public interface WarehouseDashboardService {
    WarehouseDashboardDto getDashboardData(Integer managerUserId);
}
