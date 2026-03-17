package edu.fpt.groupfive.dto.warehouse.response;

import lombok.Data;

@Data
public class ZoneResponse {
    private Integer zoneId;
    private String zoneName;
    private String assetTypeName;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private double usagePercentage;
}
