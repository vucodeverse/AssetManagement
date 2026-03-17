package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AssetPlacementDAO;
import edu.fpt.groupfive.model.warehouse.AssetPlacement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetPlacementDAOImpl implements AssetPlacementDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AssetPlacement> rowMapper = (rs, rowNum) -> new AssetPlacement(
            rs.getInt("asset_id"),
            rs.getInt("zone_id"),
            rs.getInt("placed_by"),
            rs.getTimestamp("placed_at").toLocalDateTime(),
            rs.getString("note")
    );

    @Override
    public void insert(AssetPlacement placement) {
        String sql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at, note) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, placement.getAssetId(), placement.getZoneId(), placement.getPlacedBy(), 
                Timestamp.valueOf(placement.getPlacedAt()), placement.getNote());
    }

    @Override
    public void delete(Integer assetId) {
        String sql = "DELETE FROM wh_asset_placement WHERE asset_id = ?";
        jdbcTemplate.update(sql, assetId);
    }

    @Override
    public Optional<AssetPlacement> findByAssetId(Integer assetId) {
        String sql = "SELECT * FROM wh_asset_placement WHERE asset_id = ?";
        List<AssetPlacement> results = jdbcTemplate.query(sql, rowMapper, assetId);
        return results.stream().findFirst();
    }
}
