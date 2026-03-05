package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.Warehouse;
import java.util.List;

public interface WarehouseDAO {
    Warehouse findById(Integer id);

    List<Warehouse> findAll();

    int insert(Warehouse warehouse);

    int update(Warehouse warehouse);
}
