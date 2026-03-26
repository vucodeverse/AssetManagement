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
    private Integer assetTypeId;
    private  Integer purchaseOrderDetailId;
    private Integer receiptDetailId;
    private AssetStatus currentStatus;
    private BigDecimal originalCost;
    private Integer departmentId;
    private LocalDate acquisitionDate;
    private LocalDate inServiceDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;

    private String assetTypeName;

    // Thêm cho truy vấn trả tài sản
    private String note;

}
