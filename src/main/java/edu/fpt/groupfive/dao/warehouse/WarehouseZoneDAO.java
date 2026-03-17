package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import java.util.List;
import java.util.Optional;

public interface WarehouseZoneDAO {
    void insert(WarehouseZone zone);
    void update(WarehouseZone zone);
    Optional<WarehouseZone> findById(Integer zoneId);
    List<WarehouseZone> findByWarehouseId(Integer warehouseId);
    List<WarehouseZone> findAvailableZones(Integer assetTypeId, int requiredCapacity);
}
