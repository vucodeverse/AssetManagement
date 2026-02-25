package edu.fpt.groupfive.dto.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record WarehouseReqDto(

        @NotBlank
        String name,
        @NotBlank
        String address,

        int managerId

) {
}
