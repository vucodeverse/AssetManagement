package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.Zone;
import java.util.List;

public interface ZoneDAO {
    Zone findById(Integer id);

    List<Zone> findByWarehouseId(Integer warehouseId);

    int insert(Zone zone);

    int update(Zone zone);

    int updateCapacity(Integer id, Integer currentCapacity);
}
