package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequestDetailRequest {
    @NotNull(message = "Loại tài sản không được để trống!")
    private Integer assetTypeId;

    @Min(value = 1, message = "Số lượng phải >= 1")
    @NotNull(message = "Số lượng không được để trống")
    private Integer requestedQuantity;

    @Pattern(regexp = "^[\\p{L}a-zA-Z0-9 .,_-]+$", message = "Ghi chú cấp không được chứa kí tự đặc biệt")
    private String note;
}
