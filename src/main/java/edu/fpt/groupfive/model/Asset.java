package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.AssetStatus;
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
    private String assetName;
    private String serialNumber;
    private AssetStatus currentStatus;
    private BigDecimal originalCost;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private LocalDate acquisitionDate;
    private Integer assetTypeId;
    private  Integer purchaseOrderDetailId;

    private Integer departmentId;
    private Integer goodsReceiptId;

    private LocalDate inServiceDate;

    private String assetTypeName;
}
