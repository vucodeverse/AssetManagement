package edu.fpt.groupfive.dto.warehouse.response;

import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ZoneResponseDto {
    private Integer id;
    private Integer warehouseId;
    private String warehouseName;
    private String name;
    private Integer assignedAssetTypeId;
    private String assignedAssetTypeName;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private ActiveStatus status;

    public boolean isActive() {
        return status == ActiveStatus.ACTIVE;
    }

    public int getFreeCapacity() {
        int current = currentCapacity != null ? currentCapacity : 0;
        int max = maxCapacity != null ? maxCapacity : 0;
        return max - current;
    }
}
