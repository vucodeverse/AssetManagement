package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.AssetLocation;

import java.util.List;

public interface AssetLocationDAO {

    List<AssetLocation> findByZoneId(Integer zoneId);

    void batchUpsert(List<AssetLocation> locations);

    void deleteByTicketId(Integer ticketId);
}
