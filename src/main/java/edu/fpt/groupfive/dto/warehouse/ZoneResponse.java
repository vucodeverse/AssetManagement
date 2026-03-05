package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {
    private Integer id;
    private Integer warehouseId;
    private String name;
    private Integer assignedAssetTypeId;
    private Integer maxCapacity;
    private Integer currentCapacity;
}
