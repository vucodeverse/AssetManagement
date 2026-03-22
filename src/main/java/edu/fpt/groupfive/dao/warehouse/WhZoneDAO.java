package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;

import java.util.List;
import java.util.Optional;

public interface WhZoneDAO {
    List<ZoneCapacityResponseDTO> getAllZonesWithCapacity();
    
    Optional<ZoneCapacityResponseDTO> getZoneById(int zoneId);
    
    void updateZone(int zoneId, String zoneName, int maxCapacity);
    
    void createZone(WarehouseZone zone);

    void updateCurrentCapacity(int assetTypeId, int unitVolume);

    Optional<AssetLocationResponseDTO> getAssetLocation(int assetId);

    void updateCurrentCapacityForDecrease(int zoneId, int unitVolume);
}
