package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;

import java.util.List;

public interface WarehouseService {
    Warehouse createWarehouse(WarehouseReqDto request);

    Warehouse updateWarehouse(Integer id, WarehouseReqDto request);

    void deleteWarehouse(Integer id);

    Warehouse getWarehouse(Integer id);

    //TODO: get warehouses by manager id.
    List<Warehouse> getAllWarehouses();
}
