package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDetailCreateRequest {
    private Integer purchaseRequestDetailId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng không được nhỏ hơn 1")
    private Integer quantity;

    @Pattern(regexp = "^[a-zA-Z0-9À-ỹ ]*$", message = "Không được chứa ký tự đặc biệt")
    @Size(max = 255, message = "Không được điền quá 255 kí tự")
    private String quotationDetailNote;

    @NotNull(message = "Thời gian bảo hành không được để trống")
    @Min(value = 0, message = "Thời lượng bảo hành không được nhỏ hơn 0")
    private Integer warrantyMonths;

    @NotNull(message = "Giá của sản phẩm không được để trôống")
    @DecimalMin(value = "0.0", message = "Giá của sản phẩm không được nhở hơn 0")
    private BigDecimal price;

    @NotNull(message = "Thuế của sản phẩm không được để trống")
    @DecimalMin(value = "0.0", message = "Thuế của sản phẩm không được nhở hơn 0")
    private BigDecimal taxRate;

    @DecimalMin(value = "0.0", message = "Giảm giá của sản phẩm không được nhở hơn 0")
    private BigDecimal discountRate;

    @NotNull(message = "Tên của sản phẩm không được để trống")
    private String assetTypeName;

    @Pattern(regexp = "^[a-zA-Z0-9À-ỹ ]*$", message = "Không được chứa ký tự đặc biệt")
    @Size(min = 1,max = 255, message = "Không được điền quá 255 kí tự")
    @NotBlank(message = "Không được để trống")
    private String specificationRequirement;
}
