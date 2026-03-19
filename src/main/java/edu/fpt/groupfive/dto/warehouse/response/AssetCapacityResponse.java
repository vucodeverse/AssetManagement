package edu.fpt.groupfive.dto.warehouse.response;

import lombok.Data;

@Data
public class AssetCapacityResponse {
    private Integer assetTypeId;
    private String typeName;
    private String categoryName;
    private Integer unitVolume;
}
