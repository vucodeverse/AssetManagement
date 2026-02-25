package edu.fpt.groupfive.dto.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseStatus;

public record WarehouseRespDto(
        int id,
        String name,
        String address,
        WarehouseStatus status,

) {
}
