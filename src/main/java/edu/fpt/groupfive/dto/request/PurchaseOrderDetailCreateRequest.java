package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDetailCreateRequest {

    @Min(value = 1, message = "Số lượng sản phẩm không được < 1")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Giá của sản phẩm không được < 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Thuế không được < 0")
    private BigDecimal taxRate;

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer assetTypeId;
    private Integer quotationDetailId;

    @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Không được chứa ký tự đặc biệt")
    private String orderDetailNote;

    @Min(value = 0, message = "Giảm giá không đượcc <0")
    private BigDecimal discountRate;
    private String assetTypeName;
}
