package edu.fpt.groupfive.dto.warehouse.request;

import lombok.Data;

@Data
public class AssetCapacityRequest {
    private Integer assetTypeId;
    private Integer unitVolume;
}
