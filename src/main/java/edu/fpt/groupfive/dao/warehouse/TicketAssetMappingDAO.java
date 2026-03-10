package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;

public interface TicketAssetMappingDAO {
    void insert(TicketAssetMapping mapping);

    int countUnmatchedDetails(Integer ticketId);
}
