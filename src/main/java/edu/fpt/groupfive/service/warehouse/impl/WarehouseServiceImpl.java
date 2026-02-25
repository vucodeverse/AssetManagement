package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Warehouse updateWarehouse(Integer id, WarehouseReqDto request) {

        Warehouse warehouse = getWarehouse(id);

        if (!warehouse.isArchive()) {
            throw new RuntimeException(
                    String.format("Warehouse with id %d has been deleted", id)
            );
        }

        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setManagerId(request.managerId());
        return warehouseDAO.update(warehouse);

    }

    @Override
    public void deleteWarehouse(Integer id) {
        if (!warehouseDAO.existsById(id)) {
            throw new RuntimeException(String.format("Warehouse with id %d not found", id));
        }
        warehouseDAO.deleteById(id);
    }

    public void archiveWarehouse(Integer id) {
        Warehouse warehouse = getWarehouse(id);
        if (warehouse.isArchive()) {
            throw new RuntimeException(String.format("Warehouse with id %d has been archived", id));
        }
        warehouse.archive();
        warehouseDAO.update(warehouse);
    }

    @Override
    public Warehouse getWarehouse(Integer id) {
        return warehouseDAO.findById(id).orElseThrow(
                () -> new RuntimeException(
                        String.format("Warehouse with id %d not found", id)
                )
        );
    }

    //TODO: get warehouses by manager id.
    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseDAO.findAll();
    }


}
