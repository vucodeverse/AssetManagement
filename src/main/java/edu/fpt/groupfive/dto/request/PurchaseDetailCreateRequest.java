package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailCreateRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Đặc tả Không được để trống")
    private String specificationRequirement;

    private String purchaseDetailNote;

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer assetTypeId;

    @NotNull(message = "Giá ước tính Không được để trống")
    @Min(value = 1, message = "Giá ước tính phải lớn hơn 0")
    private BigDecimal estimatePrice;
}
