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
public class AuditScanResponse {
    private Integer id;
    private Integer auditId;
    private Integer assetId;
    private String matchStatus;
    private LocalDateTime scannedAt;
    private String actionTaken;
}
