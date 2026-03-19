package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhZoneDAO;
import edu.fpt.groupfive.dto.request.warehouse.ZoneCreateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhZoneServiceImpl implements WhZoneService {

    private final WhZoneDAO whZoneDAO;

    @Override
    public List<ZoneCapacityResponseDTO> getAllZones() {
        return whZoneDAO.getAllZonesWithCapacity();
    }

    @Override
    public ZoneCapacityResponseDTO getZoneById(int zoneId) {
        return whZoneDAO.getZoneById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy zone có ID: " + zoneId));
    }

    @Override
    public void updateZone(int zoneId, ZoneCreateRequestDTO dto) {
        whZoneDAO.updateZone(zoneId, dto.getZoneName(), dto.getMaxCapacity());
    }

    @Override
    public void createZone(ZoneCreateRequestDTO dto) {
        WarehouseZone zone = new WarehouseZone();
        zone.setWarehouseId(1); // Mặc định kho số 1
        zone.setZoneName(dto.getZoneName());
        zone.setMaxCapacity(dto.getMaxCapacity());
        zone.setStatus("ACTIVE");
        whZoneDAO.createZone(zone);
    }
}
