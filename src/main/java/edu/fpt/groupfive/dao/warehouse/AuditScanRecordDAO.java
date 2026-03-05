package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import java.util.List;

public interface AuditScanRecordDAO {
    int insert(AuditScanRecord record);

    List<AuditScanRecord> findByAuditId(Integer auditId);
}
