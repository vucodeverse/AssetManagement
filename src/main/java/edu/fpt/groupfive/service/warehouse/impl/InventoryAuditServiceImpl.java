package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryAuditDAO;
import edu.fpt.groupfive.dto.warehouse.AuditCreateRequest;
import edu.fpt.groupfive.dto.warehouse.AuditResponse;
import edu.fpt.groupfive.dto.warehouse.AuditUpdateRequest;
import edu.fpt.groupfive.mapper.warehouse.InventoryAuditMapper;
import edu.fpt.groupfive.model.warehouse.InventoryAudit;
import edu.fpt.groupfive.service.warehouse.InventoryAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryAuditServiceImpl implements InventoryAuditService {

    @Autowired
    private InventoryAuditDAO auditDAO;

    @Autowired
    private InventoryAuditMapper auditMapper;

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
        if ("COMPLETED".equals(request.getStatus()) || "CANCELLED".equals(request.getStatus())) {
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
