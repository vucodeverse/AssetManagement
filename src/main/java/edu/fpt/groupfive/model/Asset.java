package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    private Integer assetId;
    private String serialNumber;
    private String currentStatus;
    private BigDecimal originalCost;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;

    private LocalDate acquisitionDate;
    private Integer assetTypeId;
    private Integer shelfId;
    private Integer departmentId;
    private Integer goodsReceiptId;
    private Integer warehouseId;
    private LocalDate inServiceDate;
    private String assetTypeName;
}
