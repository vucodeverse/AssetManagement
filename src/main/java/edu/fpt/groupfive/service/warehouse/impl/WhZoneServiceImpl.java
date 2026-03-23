package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhZoneDAO;
import edu.fpt.groupfive.dto.request.warehouse.ZoneCreateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
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
        ZoneCapacityResponseDTO zone = whZoneDAO.getZoneById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy zone có ID: " + zoneId));
        zone.setAssets(whZoneDAO.getAssetsByZoneId(zoneId));
        return zone;
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

    @Override
    public void recalculateCapacityByAssetType(int assetTypeId, int unitVolume) {
        whZoneDAO.updateCurrentCapacity(assetTypeId, unitVolume);
    }

    @Override
    public void deleteZone(int zoneId) {
        ZoneCapacityResponseDTO zone = getZoneById(zoneId);
        if (zone.getCurrentCapacity() != null && zone.getCurrentCapacity() > 0) {
            throw new IllegalArgumentException("Không thể xóa Zone đang chứa tài sản (Sức chứa hiện tại > 0).");
        }
        whZoneDAO.deleteZone(zoneId);
    }

    @Override
    public AssetLocationResponseDTO findAssetLocation(String assetCode) {
        int assetId;
        try {
            assetId = Integer.parseInt(assetCode);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã tài sản không hợp lệ. Vui lòng nhập số.");
        }

        AssetLocationResponseDTO dto = whZoneDAO.getAssetLocation(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài sản với mã " + assetCode));

        if (dto.getZoneName() == null) {
            throw new IllegalArgumentException("Tài sản không có trong kho");
        }

        // Tái sử dụng setAssetCode nếu UI cần hiển thị lại
        dto.setAssetCode(assetCode);

        return dto;
    }

    @Override
    public void decreaseCapacity(Integer zoneId, Integer unitVolume) {
        whZoneDAO.updateCurrentCapacityForDecrease(zoneId, unitVolume);
    }
}
