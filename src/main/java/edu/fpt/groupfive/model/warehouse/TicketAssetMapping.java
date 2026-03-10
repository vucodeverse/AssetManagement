package edu.fpt.groupfive.model.warehouse;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketAssetMapping {
    private Integer detailId;
    private Integer assetId;
    private Integer qcReportId;
    private LocalDateTime updatedAt;
}
