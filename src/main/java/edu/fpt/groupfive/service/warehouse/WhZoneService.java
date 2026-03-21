package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.ZoneCreateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;

import java.util.List;

public interface WhZoneService {
    List<ZoneCapacityResponseDTO> getAllZones();
    
    ZoneCapacityResponseDTO getZoneById(int zoneId);
    
    void updateZone(int zoneId, ZoneCreateRequestDTO dto);
    
    void createZone(ZoneCreateRequestDTO dto);

    void recalculateCapacityByAssetType(int assetTypeId, int unitVolume);
}
