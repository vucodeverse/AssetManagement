package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.ZoneRequest;
import edu.fpt.groupfive.dto.warehouse.response.TransactionHistoryResponse;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseDashboardResponse;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponse;
import java.util.List;

public interface WarehouseService {
    WarehouseDashboardResponse getDashboardStats();
    List<ZoneResponse> getAllZones();
    void createZone(ZoneRequest request);
    void deleteZone(Integer zoneId);
    void resetZone(Integer zoneId);
    List<TransactionHistoryResponse> getTransactionHistory();
}
