package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.warehouse.WarehouseCreateRequest;
import edu.fpt.groupfive.dto.warehouse.WarehouseResponse;
import edu.fpt.groupfive.dto.warehouse.WarehouseUpdateRequest;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDAO warehouseDAO;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public WarehouseResponse createWarehouse(WarehouseCreateRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .address(request.getAddress())
                .managerUserId(request.getManagerUserId())
                .status("ACTIVE") // Default status
                .build();
        warehouseDAO.insert(warehouse);
        return warehouseMapper.toResponse(warehouse); // Currently doesn't set ID after insert, ideally DAO insert
                                                      // should return generated ID
    }

    @Override
    public WarehouseResponse updateWarehouse(WarehouseUpdateRequest request) {
        Warehouse existing = warehouseDAO.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("Warehouse not found");
        }

        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setManagerUserId(request.getManagerUserId());
        existing.setStatus(request.getStatus());

        warehouseDAO.update(existing);

        return warehouseMapper.toResponse(existing);
    }

    @Override
    public WarehouseResponse getWarehouseById(Integer id) {
        Warehouse warehouse = warehouseDAO.findById(id);
        if (warehouse == null) {
            throw new RuntimeException("Warehouse not found");
        }
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    public List<WarehouseResponse> getAllWarehouses() {
        List<Warehouse> list = warehouseDAO.findAll();
        return warehouseMapper.toResponseList(list);
    }
}
