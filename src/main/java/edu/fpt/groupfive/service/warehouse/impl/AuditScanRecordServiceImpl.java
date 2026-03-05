package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AuditScanRecordDAO;
import edu.fpt.groupfive.dto.warehouse.AuditScanRequest;
import edu.fpt.groupfive.dto.warehouse.AuditScanResponse;
import edu.fpt.groupfive.mapper.warehouse.AuditScanRecordMapper;
import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import edu.fpt.groupfive.service.warehouse.AuditScanRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditScanRecordServiceImpl implements AuditScanRecordService {

    @Autowired
    private AuditScanRecordDAO scanRecordDAO;

    @Autowired
    private AuditScanRecordMapper scanRecordMapper;

    @Override
    public AuditScanResponse scanAsset(AuditScanRequest request) {
        AuditScanRecord record = AuditScanRecord.builder()
                .auditId(request.getAuditId())
                .assetId(request.getAssetId())
                .matchStatus(request.getMatchStatus())
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
