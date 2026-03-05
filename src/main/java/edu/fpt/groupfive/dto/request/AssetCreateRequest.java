package edu.fpt.groupfive.dto.request;


import edu.fpt.groupfive.common.AssetStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetCreateRequest {

    @NotBlank(message = "Không được để trống tên tài sản")
    private String assetName;

    @Min(1)
    private Integer quantity;

    private String serialNumber;

    private Integer purchaseOrderDetailId;

    @NotNull(message = "Phải chọn loại tài sản")
    private Integer assetTypeId;

    @NotNull(message = "Không được để trống nguyên giá")
    private BigDecimal originalCost;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyStartDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyEndDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate acquisitionDate;
    @NotNull(message = "Không được để trống trạng thái tài sản")
    private AssetStatus currentStatus;
}