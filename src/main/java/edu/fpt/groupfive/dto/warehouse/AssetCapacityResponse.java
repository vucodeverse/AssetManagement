package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCapacityResponse {
    private Integer id;
    private Integer assetTypeId;
    private Integer capacityUnits;
}
