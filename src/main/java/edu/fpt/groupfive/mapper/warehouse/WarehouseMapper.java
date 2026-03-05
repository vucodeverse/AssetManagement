package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseResponse;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WarehouseMapper {

    public WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null)
            return null;
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .managerUserId(warehouse.getManagerUserId())
                .status(warehouse.getStatus())
                .build();
    }

    public List<WarehouseResponse> toResponseList(List<Warehouse> warehouses) {
        if (warehouses == null)
            return new ArrayList<>();
        List<WarehouseResponse> responseList = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            responseList.add(toResponse(warehouse));
        }
        return responseList;
    }
}
