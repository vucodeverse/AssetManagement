package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {

    private Integer assetId;
    private String assetName;
    private  Integer purchaseOrderDetailId;
    private String currentStatus;

    private LocalDate warrantyStartDate;

    private LocalDate warrantyEndDate;

    private BigDecimal originalCost;

    private Integer assetTypeId;
    private String assetTypeName;
    private LocalDate acquisitionDate;


}