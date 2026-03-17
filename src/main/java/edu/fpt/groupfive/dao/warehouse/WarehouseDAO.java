package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseDAO {
    void insert(Warehouse warehouse);
    void update(Warehouse warehouse);
    Optional<Warehouse> findById(Integer warehouseId);
    List<Warehouse> findAll();
}
