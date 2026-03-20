package edu.fpt.groupfive.dto.request.warehouse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class POItemReceiveRequestDTO {

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer assetTypeId;

    @NotNull(message = "ID chi tiết đơn hàng không được để trống")
    private Integer purchaseOrderDetailId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer actualQuantity;
}
