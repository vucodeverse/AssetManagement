package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseZone {
    private Integer zoneId;
    private Integer warehouseId;
    private String zoneName;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private Integer assetTypeId;
    private String status;
}
