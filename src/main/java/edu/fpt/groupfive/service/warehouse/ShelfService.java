package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.ShelfReqDto;
import edu.fpt.groupfive.dto.warehouse.ShelfRespDto;
import edu.fpt.groupfive.model.warehouse.Shelf;

import java.util.List;

public interface ShelfService {

    Shelf createShelf(Integer rackId, ShelfReqDto request);

    Shelf getShelf(Integer id);

    ShelfRespDto getShelfDetail(Integer id);

    List<ShelfRespDto> getAllShelvesByRack(Integer rackId);

    Shelf updateShelf(Integer id, ShelfReqDto request);

    void deleteShelf(Integer id);
}
