package edu.fpt.groupfive.dto.warehouse;

public record RackRespDto(
        int id,
        String name,
        String description,
        String status,
        boolean active,
        int warehouseId,
        String warehouseName,
        int shelfCount
) {
}
