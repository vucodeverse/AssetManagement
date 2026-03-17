package edu.fpt.groupfive.dto.warehouse.request;

import lombok.Data;

@Data
public class ZoneRequest {
    private Integer warehouseId;
    private String zoneName;
    private Integer maxCapacity;
    private Integer assetTypeId;
}
