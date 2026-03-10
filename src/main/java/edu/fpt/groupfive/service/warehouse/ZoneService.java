package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.request.ZoneRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponseDto;
import edu.fpt.groupfive.mapper.warehouse.ZoneMapper;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.util.exception.ZoneNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneDAO zoneDAO;
    private final ZoneMapper zoneMapper;
    private final WarehouseDAO warehouseDAO;
    private final AssetTypeDAO assetTypeDAO;

    public List<ZoneResponseDto> getAllZones() {
        List<Zone> zones = zoneDAO.findAll();
        return enrichList(zones);
    }

    public List<ZoneResponseDto> getZonesByWarehouseId(Integer warehouseId) {
        List<Zone> zones = zoneDAO.findByWarehouseId(warehouseId);
        return enrichList(zones);
    }

    public ZoneResponseDto getZoneById(Integer id) {
        Zone zone = zoneDAO.findById(id);
        return enrich(zone);
    }

    public ZoneResponseDto createZone(ZoneRequestDto dto) {
        Zone zone = zoneMapper.toEntity(dto);
        zone.setCurrentCapacity(0);
        zone.setStatus(ActiveStatus.ACTIVE);
        Zone saved = zoneDAO.create(zone);
        return enrich(saved);
    }

    public ZoneResponseDto updateZone(Integer id, ZoneRequestDto dto) {
        Zone existing = zoneDAO.findById(id); // ném ZoneNotFoundException nếu không tìm thấy
        existing.setName(dto.getName());
        existing.setMaxCapacity(dto.getMaxCapacity());
        Zone updated = zoneDAO.update(existing);
        return enrich(updated);
    }

    public void toggleStatus(Integer id, ActiveStatus status) {
        zoneDAO.findById(id); // ném ZoneNotFoundException nếu không tìm thấy
        zoneDAO.updateStatus(id, status);
    }

    // Làm giàu thông tin DTO với tên kho và tên loại tài sản
    private ZoneResponseDto enrich(Zone zone) {
        ZoneResponseDto dto = zoneMapper.toResp(zone);

        // Lấy tên kho
        try {
            Warehouse warehouse = warehouseDAO.getById(zone.getWarehouseId());
            dto.setWarehouseName(warehouse.getName());
        } catch (Exception e) {
            dto.setWarehouseName("N/A");
        }

        // Lấy tên loại tài sản (nếu có)
        if (zone.getAssignedAssetTypeId() != null) {
            try {
                AssetType assetType = assetTypeDAO.findById(zone.getAssignedAssetTypeId());
                if (assetType != null) {
                    dto.setAssignedAssetTypeName(assetType.getTypeName());
                }
            } catch (Exception e) {
                dto.setAssignedAssetTypeName("N/A");
            }
        }

        return dto;
    }

    private List<ZoneResponseDto> enrichList(List<Zone> zones) {
        return zones.stream().map(this::enrich).toList();
    }
}
