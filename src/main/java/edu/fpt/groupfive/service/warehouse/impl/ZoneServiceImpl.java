package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.ZoneCreateRequest;
import edu.fpt.groupfive.dto.warehouse.ZoneResponse;
import edu.fpt.groupfive.dto.warehouse.ZoneUpdateRequest;
import edu.fpt.groupfive.mapper.warehouse.ZoneMapper;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.service.warehouse.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    private ZoneDAO zoneDAO;

    @Autowired
    private ZoneMapper zoneMapper;

    @Override
    public ZoneResponse createZone(ZoneCreateRequest request) {
        Zone zone = Zone.builder()
                .warehouseId(request.getWarehouseId())
                .name(request.getName())
                .assignedAssetTypeId(request.getAssignedAssetTypeId())
                .maxCapacity(request.getMaxCapacity())
                .currentCapacity(0) // Default when creating new zone
                .build();
        zoneDAO.insert(zone);
        return zoneMapper.toResponse(zone);
    }

    @Override
    public ZoneResponse updateZone(ZoneUpdateRequest request) {
        Zone existing = zoneDAO.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("Zone not found");
        }
        existing.setWarehouseId(request.getWarehouseId());
        existing.setName(request.getName());
        existing.setAssignedAssetTypeId(request.getAssignedAssetTypeId());
        existing.setMaxCapacity(request.getMaxCapacity());
        existing.setCurrentCapacity(request.getCurrentCapacity());

        zoneDAO.update(existing);
        return zoneMapper.toResponse(existing);
    }

    @Override
    public ZoneResponse getZoneById(Integer id) {
        Zone zone = zoneDAO.findById(id);
        if (zone == null) {
            throw new RuntimeException("Zone not found");
        }
        return zoneMapper.toResponse(zone);
    }

    @Override
    public List<ZoneResponse> getZonesByWarehouseId(Integer warehouseId) {
        List<Zone> zones = zoneDAO.findByWarehouseId(warehouseId);
        return zoneMapper.toResponseList(zones);
    }
}
