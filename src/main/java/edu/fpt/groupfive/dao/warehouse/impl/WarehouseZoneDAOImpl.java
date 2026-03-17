package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseZoneDAO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseZoneDAOImpl implements WarehouseZoneDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<WarehouseZone> rowMapper = (rs, rowNum) -> new WarehouseZone(
            rs.getInt("zone_id"),
            rs.getInt("warehouse_id"),
            rs.getString("zone_name"),
            rs.getInt("max_capacity"),
            rs.getInt("current_capacity"),
            rs.getObject("asset_type_id", Integer.class),
            rs.getString("status")
    );

    @Override
    public void insert(WarehouseZone zone) {
        String sql = "INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, zone.getWarehouseId(), zone.getZoneName(), zone.getMaxCapacity(), zone.getCurrentCapacity(), zone.getAssetTypeId(), zone.getStatus());
    }

    @Override
    public void update(WarehouseZone zone) {
        String sql = "UPDATE wh_zones SET warehouse_id = ?, zone_name = ?, max_capacity = ?, current_capacity = ?, asset_type_id = ?, status = ? WHERE zone_id = ?";
        jdbcTemplate.update(sql, zone.getWarehouseId(), zone.getZoneName(), zone.getMaxCapacity(), zone.getCurrentCapacity(), zone.getAssetTypeId(), zone.getStatus(), zone.getZoneId());
    }

    @Override
    public Optional<WarehouseZone> findById(Integer zoneId) {
        String sql = "SELECT * FROM wh_zones WHERE zone_id = ?";
        List<WarehouseZone> results = jdbcTemplate.query(sql, rowMapper, zoneId);
        return results.stream().findFirst();
    }

    @Override
    public List<WarehouseZone> findByWarehouseId(Integer warehouseId) {
        String sql = "SELECT * FROM wh_zones WHERE warehouse_id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }

    @Override
    public List<WarehouseZone> findAvailableZones(Integer assetTypeId, int requiredCapacity) {
        // Find zones that are empty or contain the same asset type and have enough capacity
        String sql = "SELECT * FROM wh_zones WHERE status = 'ACTIVE' " +
                     "AND (asset_type_id IS NULL OR asset_type_id = ?) " +
                     "AND (max_capacity - current_capacity) >= ?";
        return jdbcTemplate.query(sql, rowMapper, assetTypeId, requiredCapacity);
    }
}
