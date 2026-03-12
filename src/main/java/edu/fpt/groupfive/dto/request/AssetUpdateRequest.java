package edu.fpt.groupfive.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AssetUpdateRequest {

    private Integer assetId;

    private String serialNumber;

    private String currentStatus;

    private LocalDate warrantyStartDate;

    private LocalDate warrantyEndDate;

    private BigDecimal originalCost;

    private Integer assetTypeId;

    private LocalDate acquisitionDate;
}