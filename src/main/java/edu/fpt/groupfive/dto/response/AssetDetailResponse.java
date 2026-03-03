package edu.fpt.groupfive.dto.response;

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
public class AssetDetailResponse {
    private Integer assetId;
    private String assetName;
    private String serialNumber;
    private String assetTypeName;

    private String warehouseName;
    private String rackName;
    private String shelfName;

    private LocalDate acquisitionDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;


    private String departmentName;
    private LocalDate allocationDate;

    private BigDecimal originalCost;
}
