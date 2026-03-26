package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetPlacementResponseDTO {
    private String assetCode;
    private String zoneName;
    private String floor;
}
