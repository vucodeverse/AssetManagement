package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.Zone;
import java.util.List;

public interface ZoneDAO {
    List<Zone> findByWarehouseId(Integer warehouseId);

    int insert(Zone zone);

    int updateCapacity(Integer id, Integer currentCapacity);
}
