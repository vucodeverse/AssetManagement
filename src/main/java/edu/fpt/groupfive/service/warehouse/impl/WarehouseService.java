package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.warehouse.request.WarehouseRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseResponseDTO;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.util.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseDAO warehouseDAO;
    private final WarehouseMapper warehouseMapper;

    public WarehouseResponseDTO addWarehouse(WarehouseRequestDto dto) {
        Warehouse warehouse = warehouseMapper.toEntity(dto);
        warehouse.setStatus(ActiveStatus.ACTIVE);
        return warehouseMapper.toResp(warehouseDAO.create(warehouse));
    }

    public WarehouseResponseDTO updateWarehouse(Integer id, WarehouseRequestDto dto) {
        // Kiểm tra kho tồn tại trước
        warehouseDAO.getById(id); // ném WarehouseNotFoundException nếu không tìm thấy
        Warehouse warehouse = warehouseMapper.toEntity(dto);
        warehouse.setId(id);
        return warehouseMapper.toResp(warehouseDAO.update(warehouse));
    }

    public void changeActiveStatus(Integer id, ActiveStatus status) {
        warehouseDAO.getById(id); // ném WarehouseNotFoundException nếu không tìm thấy
        warehouseDAO.setActiveStatus(id, status);
    }

    public List<WarehouseResponseDTO> getAllWarehouse() {
        return warehouseMapper.toListResp(warehouseDAO.findAll());
    }

    public WarehouseResponseDTO getWarehouse(Integer id) {
        Warehouse warehouse = warehouseDAO.getById(id);
        return warehouseMapper.toResp(warehouse);
    }

    public WarehouseResponseDTO getWarehouseByManager(Integer userId) {
        Warehouse warehouse = warehouseDAO.getByManager(userId);
        return warehouseMapper.toResp(warehouse);
    }
}
