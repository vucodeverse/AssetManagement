package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    private Integer id;
    private Integer warehouseId;
    private String name;
    private Integer assignedAssetTypeId;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private String status;
}
