package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.ShelfDAO;
import edu.fpt.groupfive.dto.warehouse.ShelfReqDto;
import edu.fpt.groupfive.dto.warehouse.ShelfRespDto;
import edu.fpt.groupfive.model.warehouse.Shelf;
import edu.fpt.groupfive.service.warehouse.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelfServiceImpl implements ShelfService {

    private final ShelfDAO shelfDAO;

    @Override
    public Shelf createShelf(Integer rackId, ShelfReqDto request) {
        Shelf shelf = new Shelf();
        shelf.setRackId(rackId);
        shelf.setName(request.name());
        shelf.setMaxCapacity(request.maxCapacity());
        shelf.setDescription(request.description());
        shelf.setCurrentCapacity(0);
        return shelfDAO.create(shelf);
    }

    @Override
    public Shelf getShelf(Integer id) {
        return shelfDAO.getById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Tầng với id %d", id)));
    }

    @Override
    public ShelfRespDto getShelfDetail(Integer id) {
        return shelfDAO.getDetailById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Tầng với id %d", id)));
    }

    @Override
    public List<ShelfRespDto> getAllShelvesByRack(Integer rackId) {
        return shelfDAO.getAllByRackId(rackId);
    }

    @Override
    public Shelf updateShelf(Integer id, ShelfReqDto request) {
        Shelf shelf = shelfDAO.getById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Không tìm thấy Tầng với id %d", id)));
        shelf.setName(request.name());
        shelf.setMaxCapacity(request.maxCapacity());
        shelf.setDescription(request.description());
        return shelfDAO.update(shelf);
    }

    @Override
    public void deleteShelf(Integer id) {
        if (!shelfDAO.existById(id)) {
            throw new RuntimeException(String.format("Không tìm thấy Tầng với id %d", id));
        }
        shelfDAO.deleteById(id);
    }
}
