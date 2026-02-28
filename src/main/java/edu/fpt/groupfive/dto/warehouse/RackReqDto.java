package edu.fpt.groupfive.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RackReqDto(
        @NotBlank(message = "Tên kệ không được để trống")
        String name,
        String description
) {
}
