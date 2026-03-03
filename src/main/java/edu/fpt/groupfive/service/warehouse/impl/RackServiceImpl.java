package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.RackDAO;
import edu.fpt.groupfive.dto.warehouse.RackReqDto;
import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.model.warehouse.Rack;
import edu.fpt.groupfive.service.warehouse.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RackServiceImpl implements RackService {

    private final RackDAO rackDAO;

    @Override
    public Rack createRack(Integer warehouseId, RackReqDto request) {
        Rack rack = new Rack();
        rack.setWarehouseId(warehouseId);
        rack.setName(request.name());
        rack.setDescription(request.description());
        return rackDAO.create(rack);
    }

    @Override
    public Rack getRack(Integer id) {
        return rackDAO.getById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Kệ với id %d", id)));
    }

    @Override
    public RackRespDto getRackDetail(Integer id) {
        return rackDAO.getDetailById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Kệ với id %d", id)));
    }

    @Override
    public List<RackRespDto> getAllRacksByWarehouse(Integer warehouseId) {
        return rackDAO.getAllByWarehouseId(warehouseId);
    }

    @Override
    public Rack updateRack(Integer id, RackReqDto request) {
        Rack rack = rackDAO.getById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Kệ với id %d", id)));
        rack.setName(request.name());
        rack.setDescription(request.description());
        return rackDAO.update(rack);
    }

    @Override
    public void deleteRack(Integer id) {
        if (!rackDAO.existById(id)) {
            throw new RuntimeException(String.format("Không tìm thấy Kệ với id %d", id));
        }
        rackDAO.deleteById(id);
    }
}
