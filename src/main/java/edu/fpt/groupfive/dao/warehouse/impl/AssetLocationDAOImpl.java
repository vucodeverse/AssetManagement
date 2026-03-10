package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AssetLocationDAO;
import edu.fpt.groupfive.model.warehouse.AssetLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetLocationDAOImpl implements AssetLocationDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AssetLocation> findByZoneId(Integer zoneId) {
        String sql = "SELECT asset_id, zone_id, last_ticket_id FROM wh_asset_location WHERE zone_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AssetLocation location = new AssetLocation();
            location.setAssetId(rs.getInt("asset_id"));
            location.setZoneId(rs.getInt("zone_id"));
            int lastTicketId = rs.getInt("last_ticket_id");
            location.setLastTicketId(rs.wasNull() ? null : lastTicketId);
            return location;
        }, zoneId);
    }

    @Override
    public void batchUpsert(List<AssetLocation> locations) {
        String sql = "IF EXISTS (SELECT 1 FROM wh_asset_location WHERE asset_id = ?) " +
                "    UPDATE wh_asset_location SET zone_id = ?, last_ticket_id = ? WHERE asset_id = ? " +
                "ELSE " +
                "    INSERT INTO wh_asset_location (asset_id, zone_id, last_ticket_id) VALUES (?, ?, ?)";

        List<Object[]> batchArgs = locations.stream()
                .map(loc -> new Object[] {
                        loc.getAssetId(),
                        loc.getZoneId(), loc.getLastTicketId(), loc.getAssetId(),
                        loc.getAssetId(), loc.getZoneId(), loc.getLastTicketId()
                })
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void deleteByTicketId(Integer ticketId) {
        String sql = "DELETE FROM wh_asset_location " +
                "WHERE asset_id IN (" +
                "    SELECT tam.asset_id " +
                "    FROM wh_ticket_asset_mapping tam " +
                "    JOIN wh_ticket_detail td ON tam.detail_id = td.id " +
                "    WHERE td.ticket_id = ?" +
                ")";
        jdbcTemplate.update(sql, ticketId);
    }
}
