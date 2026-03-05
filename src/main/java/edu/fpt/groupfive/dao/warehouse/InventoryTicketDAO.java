package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import java.util.List;

public interface InventoryTicketDAO {
    int insert(InventoryTicket ticket);

    int update(InventoryTicket ticket);

    InventoryTicket findById(Integer id);

    List<InventoryTicket> findByWarehouseId(Integer warehouseId);
}
