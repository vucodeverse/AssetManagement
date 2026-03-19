package edu.fpt.groupfive.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneCapacityResponseDTO {
    private Integer zoneId;
    private String zoneName;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private Integer assetTypeId;
    private String assetTypeName;
    private String status;

    // Computed fields
    public int getFillPercentage() {
        if (maxCapacity == null || maxCapacity == 0) return 0;
        return (int) Math.round((double) currentCapacity / maxCapacity * 100);
    }

    /**
     * EMPTY   = zone đang trống (assetTypeId IS NULL hoặc currentCapacity == 0)
     * IN_USE  = đang có hàng nhưng chưa đầy
     * FULL    = currentCapacity == maxCapacity
     */
    public String getStatusFlag() {
        if (currentCapacity == null || currentCapacity == 0) return "EMPTY";
        if (currentCapacity.equals(maxCapacity)) return "FULL";
        return "IN_USE";
    }
}
