package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.AssetStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AssetDetailResponse {

    private Integer assetId;
    private String assetName;
    private Integer assetTypeId;
    private Integer purchaseOrderDetailId;
    private AssetStatus currentStatus;
    private BigDecimal originalCost;
    private Integer departmentId;
    private LocalDate acquisitionDate;
    private LocalDate inServiceDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private String assetTypeName;
    private String departmentName;
    private  Integer purchaseOrderId;
    private LocalDate orderDate;
    private String supplierName;



}