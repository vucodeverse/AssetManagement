package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditCreateRequest;
import edu.fpt.groupfive.dto.warehouse.AuditResponse;
import edu.fpt.groupfive.dto.warehouse.AuditUpdateRequest;

import java.util.List;

public interface InventoryAuditService {
    AuditResponse createAudit(AuditCreateRequest request);

    AuditResponse updateAudit(AuditUpdateRequest request);

    AuditResponse getAuditById(Integer id);

    List<AuditResponse> getAuditsByWarehouseId(Integer warehouseId);
}
