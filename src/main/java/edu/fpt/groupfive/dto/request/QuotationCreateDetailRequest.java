package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCreateDetailRequest {
    private Integer purchaseRequestDetailId;

    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;
    private String quotationDetailNote;

    @NotNull(message = "Thời gian bảo hành không được để trống")
    private Integer warrantyMonths;

    @NotNull(message = "Giá của sản phẩm không được để trôống")
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;

    @NotNull(message = "Tên của sản phẩm không được để trống")
    private String assetTypeName;
    private String specificationRequirement;
}
