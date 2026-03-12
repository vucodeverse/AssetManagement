package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.dto.warehouse.ShelfRespDto;
import edu.fpt.groupfive.model.warehouse.Shelf;

import java.util.List;
import java.util.Optional;

public interface ShelfDAO {

    Shelf create(Shelf shelf);

    Shelf update(Shelf shelf);

    Optional<Shelf> getById(Integer id);

    Optional<ShelfRespDto> getDetailById(Integer id);

    List<ShelfRespDto> getAllByRackId(Integer rackId);

    void deleteById(Integer id);

    boolean existById(Integer id);
}
