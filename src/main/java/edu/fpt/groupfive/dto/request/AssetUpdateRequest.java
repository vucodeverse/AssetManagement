package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.AssetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AssetUpdateRequest {

    private Integer assetId;
    @NotNull(message = "Không được để trống tên tài sản")
    private String assetName;
    private String serialNumber;
    @NotNull(message = "Không được để trống trạng thái tài sản")
    private AssetStatus currentStatus;
    @NotNull(message = "Không được để trống nguyên giá")
    private BigDecimal originalCost;
    @NotNull(message = "Phải chọn loại tài sản")
    private Integer assetTypeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyStartDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyEndDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate acquisitionDate;
}