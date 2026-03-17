package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseZoneDAO;
import edu.fpt.groupfive.dto.warehouse.request.ZoneRequest;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseDashboardResponse;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponse;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseZoneDAO warehouseZoneDAO;
    private final WarehouseMapper warehouseMapper;
    // Other DAOs for stats...

    @Override
    public WarehouseDashboardResponse getDashboardStats() {
        WarehouseDashboardResponse response = new WarehouseDashboardResponse();
        // Mocking stats for now, will implement actual queries later
        response.setPendingPO(5);
        response.setPendingReturn(3);
        response.setPendingAllocation(7);
        return response;
    }

    @Override
    public List<ZoneResponse> getAllZones() {
        // For simplicity, assuming one warehouse for now
        return warehouseZoneDAO.findByWarehouseId(1).stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void createZone(ZoneRequest request) {
        WarehouseZone zone = warehouseMapper.toEntity(request);
        warehouseZoneDAO.insert(zone);
    }

    @Override
    @Transactional
    public void deleteZone(Integer zoneId) {
        warehouseZoneDAO.findById(zoneId).ifPresent(zone -> {
            zone.setStatus("INACTIVE");
            warehouseZoneDAO.update(zone);
        });
    }

    @Override
    @Transactional
    public void resetZone(Integer zoneId) {
        warehouseZoneDAO.findById(zoneId).ifPresent(zone -> {
            if (zone.getCurrentCapacity() == 0) {
                zone.setAssetTypeId(null);
                warehouseZoneDAO.update(zone);
            } else {
                throw new RuntimeException("Cannot reset zone with assets still placed.");
            }
        });
    }
}
