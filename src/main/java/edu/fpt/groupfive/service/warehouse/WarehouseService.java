package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;

import java.util.List;

public interface WarehouseService {
    Warehouse createWarehouse(WarehouseReqDto request);

}
