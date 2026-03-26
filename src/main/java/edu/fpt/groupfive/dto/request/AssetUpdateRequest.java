package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.AssetStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Tên tài sản không được để trống")
    private String assetName;

    @NotNull(message = "Trạng thái không được để trống")
    private AssetStatus currentStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyEndDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate acquisitionDate;

    private String note;
}