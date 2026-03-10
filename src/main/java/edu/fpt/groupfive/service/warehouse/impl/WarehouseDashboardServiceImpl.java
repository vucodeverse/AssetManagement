package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseDashboardDto;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.service.warehouse.WarehouseDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseDashboardServiceImpl implements WarehouseDashboardService {

    private final WarehouseDAO warehouseDAO;
    private final ZoneDAO zoneDAO;
    private final InventoryTicketDAO inventoryTicketDAO;

    @Override
    public WarehouseDashboardDto getDashboardData(Integer managerUserId) {
        Warehouse warehouse = warehouseDAO.getByManager(managerUserId);
        if (warehouse == null) {
            throw new RuntimeException("Bạn chưa được phân bổ quản lý kho nào.");
        }

        List<Zone> zones = zoneDAO.findByWarehouseId(warehouse.getId());
        List<InventoryTicket> pendingTickets = inventoryTicketDAO.findByWarehouseIdAndStatusIn(
                warehouse.getId(),
                Arrays.asList(HandleStatus.INBOX, HandleStatus.PENDING));

        int totalZones = zones.size();
        int activeZones = (int) zones.stream().filter(z -> z.getStatus() == ActiveStatus.ACTIVE).count();

        int totalCap = zones.stream().mapToInt(Zone::getMaxCapacity).sum();
        int usedCap = zones.stream().mapToInt(Zone::getCurrentCapacity).sum();

        double utilization = 0.0;
        if (totalCap > 0) {
            utilization = Math.round(((double) usedCap / totalCap) * 100.0 * 10.0) / 10.0; // Keep 1 decimal
        }

        WarehouseDashboardDto dto = new WarehouseDashboardDto();
        dto.setWarehouseName(warehouse.getName());
        dto.setTotalZones(totalZones);
        dto.setActiveZones(activeZones);
        dto.setTotalCapacity(totalCap);
        dto.setUsedCapacity(usedCap);
        dto.setUtilizationPercentage(utilization);
        dto.setPendingTickets(pendingTickets);
        dto.setPendingTicketsCount(pendingTickets.size());

        return dto;
    }
}
