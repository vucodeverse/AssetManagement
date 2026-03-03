package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.RackReqDto;
import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.model.warehouse.Rack;

import java.util.List;

public interface RackService {

    Rack createRack(Integer warehouseId, RackReqDto request);

    Rack getRack(Integer id);

    RackRespDto getRackDetail(Integer id);

    List<RackRespDto> getAllRacksByWarehouse(Integer warehouseId);

    Rack updateRack(Integer id, RackReqDto request);

    void deleteRack(Integer id);
}
