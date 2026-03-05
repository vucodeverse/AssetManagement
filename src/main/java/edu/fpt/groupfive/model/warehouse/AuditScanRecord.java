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
public class AuditScanRecord {
    private Integer id;
    private Integer auditId;
    private Integer assetId;
    private String matchStatus; // MATCHED, MISPLACED, MISSING, FOUND_UNKNOWN
    private LocalDateTime scannedAt;
    private String actionTaken;
}
