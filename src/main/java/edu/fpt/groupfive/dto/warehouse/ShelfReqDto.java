package edu.fpt.groupfive.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ShelfReqDto(
        @NotBlank(message = "Tên tầng không được để trống")
        String name,

        @NotNull(message = "Sức chứa tối đa không được để trống")
        @Min(value = 1, message = "Sức chứa tối đa phải lớn hơn 0")
        Integer maxCapacity,

        String description
) {
}
