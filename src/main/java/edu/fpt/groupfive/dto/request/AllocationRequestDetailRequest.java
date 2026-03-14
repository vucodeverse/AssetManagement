package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    private String note;
}
