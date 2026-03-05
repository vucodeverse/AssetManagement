package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditResponse;
import edu.fpt.groupfive.model.warehouse.InventoryAudit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryAuditMapper {

    public AuditResponse toResponse(InventoryAudit audit) {
        if (audit == null)
            return null;
        return AuditResponse.builder()
                .id(audit.getId())
                .warehouseId(audit.getWarehouseId())
                .zoneId(audit.getZoneId())
                .status(audit.getStatus())
                .auditorId(audit.getAuditorId())
                .startTime(audit.getStartTime())
                .endTime(audit.getEndTime())
                .note(audit.getNote())
                .build();
    }

    public List<AuditResponse> toResponseList(List<InventoryAudit> audits) {
        if (audits == null)
            return new ArrayList<>();
        List<AuditResponse> result = new ArrayList<>();
        for (InventoryAudit audit : audits) {
            result.add(toResponse(audit));
        }
        return result;
    }
}
