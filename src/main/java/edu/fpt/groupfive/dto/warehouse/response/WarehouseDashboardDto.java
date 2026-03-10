package edu.fpt.groupfive.dto.warehouse.response;

import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import lombok.Data;

import java.util.List;

@Data
public class WarehouseDashboardDto {
    private String warehouseName;
    private Integer totalZones;
    private Integer activeZones;
    private Integer totalCapacity;
    private Integer usedCapacity;
    private Double utilizationPercentage;
    private Integer pendingTicketsCount;

    // Tickets requiring attention
    private List<InventoryTicket> pendingTickets;
}
