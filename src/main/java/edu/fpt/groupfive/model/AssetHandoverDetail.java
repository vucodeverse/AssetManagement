package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetHandoverDetail {
    private Integer handoverDetailId;
    private Integer handoverId;
    private Integer assetId;
    private Integer qcReportId;
    private String note;
}
