package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Zone;

import java.util.List;

public interface ZoneDAO {

    List<Zone> findAll();

    List<Zone> findByWarehouseId(Integer warehouseId);

    Zone findById(Integer id);

    Zone create(Zone zone);

    Zone update(Zone zone);

    void updateStatus(Integer id, ActiveStatus status);
}
