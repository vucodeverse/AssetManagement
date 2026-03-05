package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditScanRequest {
    private Integer auditId;
    private Integer assetId;
    private String matchStatus; // MATCHED, MISPLACED, MISSING, FOUND_UNKNOWN
    private String actionTaken;
}
