package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LedgerRecordResponseDTO {
    private Integer transactionId;
    private String transactionType; // INBOUND, OUTBOUND, etc.
    private String assetName;
    private String zoneName;
    private String executedBy;
    private LocalDateTime executedAt;
    private Integer referenceId; // PO ID or Handover ID
    private String referenceType; // PO, HANDOVER
}
