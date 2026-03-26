package edu.fpt.groupfive.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferAssetDetailResponse {
    private Integer assetId;
    private String assetName;
    private String conditionFromSender;
    private String note;
}
