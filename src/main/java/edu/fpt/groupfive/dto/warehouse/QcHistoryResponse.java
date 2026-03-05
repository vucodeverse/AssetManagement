package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcHistoryResponse {
    private Integer id;
    private Integer ticketId;
    private Integer assetId;
    private String qcStatus;
    private Integer inspectedBy;
    private LocalDateTime qcDate;
    private String note;
}
