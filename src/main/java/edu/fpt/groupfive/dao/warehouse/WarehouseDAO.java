package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Warehouse;

import java.util.List;

public interface WarehouseDAO {
    Warehouse create(Warehouse warehouse);

    Warehouse update(Warehouse warehouse);

    void setActiveStatus(Integer id, ActiveStatus status);

    List<Warehouse> findAll();

    Warehouse getById(Integer id);

    Warehouse getByManager(Integer userId);
}
