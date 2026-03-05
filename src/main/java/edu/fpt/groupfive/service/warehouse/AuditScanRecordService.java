package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditScanRequest;
import edu.fpt.groupfive.dto.warehouse.AuditScanResponse;

import java.util.List;

public interface AuditScanRecordService {
    AuditScanResponse scanAsset(AuditScanRequest request);

    List<AuditScanResponse> getRecordsByAuditId(Integer auditId);
}
