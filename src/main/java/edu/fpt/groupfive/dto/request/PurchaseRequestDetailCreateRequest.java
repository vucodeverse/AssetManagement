package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDetailCreateRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Đặc tả Không được để trống")
    @Size(min=1, max=255, message = "Độ dài không quá 255 kí tự")
    @Pattern(regexp = "^[a-zA-Z0-9À-ỹ ]*$", message = "Không được chứa ký tự đặc biệt")
    private String specificationRequirement;

    @Size( max=255, message = "Độ dài không quá 255 kí tự")
    @Pattern(regexp = "^[a-zA-Z0-9À-ỹ ]*$", message = "Không được chứa ký tự đặc biệt")
    private String purchaseDetailNote;

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer typeId;

    @NotNull(message = "Giá ước tính Không được để trống")
    @DecimalMin(value = "0.0", message = "Giá ước tính phải lớn hơn 0")
    private BigDecimal estimatePrice;
}
