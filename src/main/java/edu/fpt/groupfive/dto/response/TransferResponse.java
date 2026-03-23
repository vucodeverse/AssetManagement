package edu.fpt.groupfive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferResponse {
    private Integer transferId;

    private Integer fromDepartmentId;
    private Integer toDepartmentId;

    private String fromDepartmentName;
    private String toDepartmentName;
    private String assetManagerName;
    private LocalDateTime createdAt;
    private String reason;
    private String status;
    private List<AssetTypeResponse> assets;
    private String senderConfirmedBy;
    private LocalDateTime senderConfirmedAt;

    private String receiverConfirmedBy;
    private LocalDateTime receiverConfirmedAt;
    private String qualityReportResult;

    private List<TransferAssetDetailResponse> transferAssets;

}
