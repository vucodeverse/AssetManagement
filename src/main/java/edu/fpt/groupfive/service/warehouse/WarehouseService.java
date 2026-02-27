package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import jakarta.validation.Valid;

import java.util.List;

public interface WarehouseService {
    Warehouse createWarehouse(WarehouseReqDto request);

    WarehouseRespDto getWarehouseDetail(Integer id);

    List<WarehouseRespDto> getAllWarehouse();

    void activeWarehouse(Integer id);

    Warehouse updateWarehouse(Integer id, @Valid WarehouseReqDto request);

    Warehouse getWarehouse(Integer id);
}
