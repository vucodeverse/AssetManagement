package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.InventoryAudit;
import java.util.List;

public interface InventoryAuditDAO {
    int insert(InventoryAudit audit);

    int update(InventoryAudit audit);

    InventoryAudit findById(Integer id);

    List<InventoryAudit> findByWarehouseId(Integer warehouseId);
}
