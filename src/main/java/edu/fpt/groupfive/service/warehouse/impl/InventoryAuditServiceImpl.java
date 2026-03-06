package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryAuditDAO;
import edu.fpt.groupfive.dto.warehouse.AuditCreateRequest;
import edu.fpt.groupfive.dto.warehouse.AuditResponse;
import edu.fpt.groupfive.dto.warehouse.AuditUpdateRequest;
import edu.fpt.groupfive.mapper.warehouse.InventoryAuditMapper;
import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import edu.fpt.groupfive.model.warehouse.InventoryAudit;
import edu.fpt.groupfive.service.warehouse.InventoryAuditService;
import edu.fpt.groupfive.dao.warehouse.AuditScanRecordDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTransactionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryAuditServiceImpl implements InventoryAuditService {

    private final InventoryAuditDAO auditDAO;
    private final AuditScanRecordDAO scanRecordDAO;
    private final InventoryTransactionDAO transactionDAO;

    private final InventoryAuditMapper auditMapper;

    @Override
    public AuditResponse createAudit(AuditCreateRequest request) {
        InventoryAudit audit = InventoryAudit.builder()
                .warehouseId(request.getWarehouseId())
                .zoneId(request.getZoneId())
                .status("IN_PROGRESS") // Initial status
                .auditorId(request.getAuditorId())
                .note(request.getNote())
                .build();
        auditDAO.insert(audit);

        List<InventoryAudit> recentAudits = auditDAO.findByWarehouseId(request.getWarehouseId());
        InventoryAudit savedAudit = recentAudits.get(0);
        return auditMapper.toResponse(savedAudit);
    }

    @Override
    public AuditResponse updateAudit(AuditUpdateRequest request) {
        InventoryAudit existing = auditDAO.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("Audit not found");
        }

        existing.setStatus(request.getStatus());
        existing.setNote(request.getNote());

        // If completed or cancelled, set end time
        if ("COMPLETED".equals(request.getStatus())) {
            List<Integer> expectedAssetIds = transactionDAO.findAssetIdsInZone(existing.getZoneId());

            List<AuditScanRecord> scans = scanRecordDAO.findByAuditId(existing.getId());
            Set<Integer> scannedIds = scans.stream().map(AuditScanRecord::getAssetId).collect(Collectors.toSet());

            for (Integer expectedId : expectedAssetIds) {
                if (!scannedIds.contains(expectedId)) {
                    AuditScanRecord missingRecord = AuditScanRecord.builder()
                            .auditId(existing.getId())
                            .assetId(expectedId)
                            .matchStatus("MISSING")
                            .actionTaken("Auto-generated upon completion")
                            .build();
                    scanRecordDAO.insert(missingRecord);
                }
            }
            existing.setEndTime(LocalDateTime.now());
        } else if ("CANCELLED".equals(request.getStatus())) {
            existing.setEndTime(LocalDateTime.now());
        }

        auditDAO.update(existing);
        return auditMapper.toResponse(existing);
    }

    @Override
    public AuditResponse getAuditById(Integer id) {
        InventoryAudit audit = auditDAO.findById(id);
        if (audit == null) {
            throw new RuntimeException("Audit not found");
        }
        return auditMapper.toResponse(audit);
    }

    @Override
    public List<AuditResponse> getAuditsByWarehouseId(Integer warehouseId) {
        return auditMapper.toResponseList(auditDAO.findByWarehouseId(warehouseId));
    }
}
