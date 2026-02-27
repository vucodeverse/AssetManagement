package edu.fpt.groupfive.dto.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseStatus;

import java.time.LocalDateTime;

public record WarehouseRespDto(
        int id,
        String name,
        String address,
        boolean active,
        String managerName
) {
}
