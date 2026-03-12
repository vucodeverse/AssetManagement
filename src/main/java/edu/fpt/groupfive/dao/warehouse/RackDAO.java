package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.model.warehouse.Rack;

import java.util.List;
import java.util.Optional;

public interface RackDAO {

    Rack create(Rack rack);

    Rack update(Rack rack);

    Optional<Rack> getById(Integer id);

    Optional<RackRespDto> getDetailById(Integer id);

    List<RackRespDto> getAllByWarehouseId(Integer warehouseId);

    void deleteById(Integer id);

    boolean existById(Integer id);
}
