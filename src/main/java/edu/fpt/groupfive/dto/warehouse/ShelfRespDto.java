package edu.fpt.groupfive.dto.warehouse;

public record ShelfRespDto(
        int id,
        String name,
        int currentCapacity,
        int maxCapacity,
        String description,
        String status,
        boolean active,
        int rackId,
        String rackName,
        int warehouseId
) {
}
