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

    @NotNull(message = "Quantity Không được để trống")
    @Min(value = 1, message = "Quantity phải lớn hơn 0")
    private Integer quantity;

    @NotBlank(message = "Specification Requirement Không được để trống")
    private String specificationRequirement;

    @NotBlank(message = "Note Không được để trống")
    private String note;

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer assetTypeId;

    @NotNull(message = "Estimate Price Không được để trống")
    @Min(value = 1, message = "Estimate Price phải lớn hơn 0")
    private BigDecimal estimatePrice;
}
