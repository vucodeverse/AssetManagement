package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketQcHistory;
import java.util.List;

public interface TicketQcHistoryDAO {
    int insert(TicketQcHistory history);

    List<TicketQcHistory> findByTicketId(Integer ticketId);

    List<TicketQcHistory> findByAssetId(Integer assetId);
}
