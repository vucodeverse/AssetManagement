package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditScanResponse;
import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuditScanRecordMapper {

    public AuditScanResponse toResponse(AuditScanRecord record) {
        if (record == null)
            return null;
        return AuditScanResponse.builder()
                .id(record.getId())
                .auditId(record.getAuditId())
                .assetId(record.getAssetId())
                .matchStatus(record.getMatchStatus())
                .scannedAt(record.getScannedAt())
                .actionTaken(record.getActionTaken())
                .build();
    }

    public List<AuditScanResponse> toResponseList(List<AuditScanRecord> records) {
        if (records == null)
            return new ArrayList<>();
        List<AuditScanResponse> result = new ArrayList<>();
        for (AuditScanRecord record : records) {
            result.add(toResponse(record));
        }
        return result;
    }
}
