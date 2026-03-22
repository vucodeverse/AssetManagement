package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WhWarehouseDAO {
    List<Warehouse> getAllWarehouses();
    Optional<Warehouse> getWarehouseById(int warehouseId);
    void createWarehouse(Warehouse warehouse);
    boolean existsAny();
}
