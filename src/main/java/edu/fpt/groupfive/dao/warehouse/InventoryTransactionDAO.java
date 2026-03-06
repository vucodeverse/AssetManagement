package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import java.util.List;

public interface InventoryTransactionDAO {
    int insert(InventoryTransaction transaction);

    List<InventoryTransaction> findByAssetId(Integer assetId);

    List<InventoryTransaction> findByTicketId(Integer ticketId);

    List<Integer> findAssetIdsInZone(Integer zoneId);
}
