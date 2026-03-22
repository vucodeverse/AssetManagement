package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssetLocationResponseDTO {
    private String assetCode;
    private String assetName;
    private String status;
    private String zoneName;
    private String placedBy;
    private LocalDateTime placedAt;
}
