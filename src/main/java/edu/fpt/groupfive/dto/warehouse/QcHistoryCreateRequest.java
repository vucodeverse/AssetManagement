package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcHistoryCreateRequest {
    private Integer ticketId;
    private Integer assetId;
    private String qcStatus;
    private Integer inspectedBy;
    private String note;
}
