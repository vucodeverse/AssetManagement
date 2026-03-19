package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.WhZoneDAO;
import edu.fpt.groupfive.dto.request.warehouse.ZoneCreateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.service.WhZoneService;
import edu.fpt.groupfive.util.exception.ZoneNotFoundException;
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
                .orElseThrow(() -> new ZoneNotFoundException("Không tìm thấy zone có ID: " + zoneId));
    }

    @Override
    public void updateZone(int zoneId, ZoneCreateRequestDTO dto) {
        ZoneCapacityResponseDTO existingZone = getZoneById(zoneId);

        if (dto.getMaxCapacity() < existingZone.getCurrentCapacity()) {
            throw new IllegalArgumentException("Sức chứa tối đa không thể nhỏ hơn không gian đang bị chiếm dụng (" + existingZone.getCurrentCapacity() + " đơn vị).");
        }

        whZoneDAO.updateZone(zoneId, dto.getZoneName(), dto.getMaxCapacity());
    }

    @Override
    public void createZone(ZoneCreateRequestDTO dto) {
        WarehouseZone zone = new WarehouseZone();
        // Giả sử có 1 warehouse_id cố định hoặc lấy từ session. Hiện tại hardcode ID = 1
        zone.setWarehouseId(1);
        zone.setZoneName(dto.getZoneName());
        zone.setMaxCapacity(dto.getMaxCapacity());
        zone.setCurrentCapacity(0);
        zone.setStatus("ACTIVE");

        whZoneDAO.createZone(zone);
    }
}
