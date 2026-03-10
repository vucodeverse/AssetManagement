package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;

import java.util.List;

public interface InventoryTicketDAO {
    List<InventoryTicket> findAllByWarehouseId(Integer warehouseId);

    List<InventoryTicket> findByWarehouseIdAndStatusIn(Integer warehouseId, List<HandleStatus> statuses);

    InventoryTicket findById(Integer id);

    InventoryTicket insert(InventoryTicket ticket);

    void update(InventoryTicket ticket);

    void updateStatus(Integer ticketId, HandleStatus status);
}
