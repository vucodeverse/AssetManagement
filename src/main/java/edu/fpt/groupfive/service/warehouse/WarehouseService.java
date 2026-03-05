package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseCreateRequest;
import edu.fpt.groupfive.dto.warehouse.WarehouseResponse;
import edu.fpt.groupfive.dto.warehouse.WarehouseUpdateRequest;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseCreateRequest request);

    WarehouseResponse updateWarehouse(WarehouseUpdateRequest request);

    WarehouseResponse getWarehouseById(Integer id);

    List<WarehouseResponse> getAllWarehouses();
}
