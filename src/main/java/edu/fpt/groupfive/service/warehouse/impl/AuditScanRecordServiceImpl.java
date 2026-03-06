package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AuditScanRecordDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryAuditDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTransactionDAO;
import edu.fpt.groupfive.dto.warehouse.AuditScanRequest;
import edu.fpt.groupfive.dto.warehouse.AuditScanResponse;
import edu.fpt.groupfive.mapper.warehouse.AuditScanRecordMapper;
import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import edu.fpt.groupfive.model.warehouse.InventoryAudit;
import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import edu.fpt.groupfive.service.warehouse.AuditScanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditScanRecordServiceImpl implements AuditScanRecordService {

    private final AuditScanRecordDAO scanRecordDAO;
    private final InventoryAuditDAO auditDAO;
    private final InventoryTransactionDAO transactionDAO;

    private final AuditScanRecordMapper scanRecordMapper;

    @Override
    public AuditScanResponse scanAsset(AuditScanRequest request) {
        // 1. Fetch Audit to get Zone ID
        InventoryAudit audit = auditDAO.findById(request.getAuditId());

        // 2. Determine actual Zone ID of Asset
        List<InventoryTransaction> txs = transactionDAO.findByAssetId(request.getAssetId());
        Integer actualZoneId = null;
        if (txs != null && !txs.isEmpty()) {
            actualZoneId = txs.get(0).getToZoneId(); // Assuming list is ordered by date DESC
        }

        // 3. Compare Zone ID
        String matchStatus = "MISPLACED";
        if (audit != null && audit.getZoneId().equals(actualZoneId)) {
            matchStatus = "MATCHED";
        }

        AuditScanRecord record = AuditScanRecord.builder()
                .auditId(request.getAuditId())
                .assetId(request.getAssetId())
                .matchStatus(matchStatus)
                .actionTaken(request.getActionTaken())
                .build();

        scanRecordDAO.insert(record);

        List<AuditScanRecord> recentRecords = scanRecordDAO.findByAuditId(request.getAuditId());
        AuditScanRecord savedRecord = recentRecords.get(0);

        return scanRecordMapper.toResponse(savedRecord);
    }

    @Override
    public List<AuditScanResponse> getRecordsByAuditId(Integer auditId) {
        return scanRecordMapper.toResponseList(scanRecordDAO.findByAuditId(auditId));
    }
}
