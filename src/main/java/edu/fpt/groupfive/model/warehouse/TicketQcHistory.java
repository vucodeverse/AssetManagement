package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketQcHistory {
    private Integer id;
    private Integer ticketId;
    private Integer assetId;
    private String qcStatus; // PASS, FAIL, NEEDS_REPAIR
    private Integer inspectedBy;
    private LocalDateTime qcDate;
    private String note;
}
