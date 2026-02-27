package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseDAO {

    Warehouse create(Warehouse newWarehouse);

    Optional<WarehouseRespDto> getDetail(Integer id);

    List<WarehouseRespDto> getAllDetail();

    void activeById(Integer id);

    boolean existById(Integer id);

    Optional<Warehouse> getById(Integer id);

    Warehouse update(Warehouse warehouse);

    Optional<WarehouseRespDto> getDetailByManagerId(Integer managerId);
}
