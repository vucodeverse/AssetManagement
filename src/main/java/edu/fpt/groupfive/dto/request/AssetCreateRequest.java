package edu.fpt.groupfive.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetCreateRequest {

    @NotBlank(message = "Không được để trống serial")
    private String serialNumber;

    @NotNull(message = "Phải chọn loại tài sản")
    private Integer assetTypeId;

    @NotNull(message = "Không được để trống nguyên giá")
    private BigDecimal originalCost;

    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private LocalDate acquisitionDate;

    private String currentStatus;
}