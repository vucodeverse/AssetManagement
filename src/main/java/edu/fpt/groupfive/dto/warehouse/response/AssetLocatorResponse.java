package edu.fpt.groupfive.dto.warehouse.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssetLocatorResponse {
    private Integer assetId;
    private String assetTag;
    private String assetName;
    private String assetTypeName;
    private String zoneName;
    private String status;
    private LocalDateTime inboundDate;
    private String executedBy;
}
