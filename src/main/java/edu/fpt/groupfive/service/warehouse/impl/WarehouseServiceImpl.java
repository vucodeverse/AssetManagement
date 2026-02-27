package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseDAO warehouseDAO;

    @Override
    public Warehouse createWarehouse(WarehouseReqDto request) {
        //TODO: check managerId is exist

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setName(request.name());
        newWarehouse.setAddress(request.address());
        newWarehouse.setManagerId(request.managerId());
        newWarehouse.deactive();

        return warehouseDAO.create(newWarehouse);
    }

    @Override
    public WarehouseRespDto getWarehouseDetail(Integer id) {
        WarehouseRespDto warehouse = warehouseDAO.getDetail(id).orElseThrow(
                ()->{throw new RuntimeException(String.format("Không tìm thấy Kho với id %d",id));}
        );
        return warehouse;
    }


}
