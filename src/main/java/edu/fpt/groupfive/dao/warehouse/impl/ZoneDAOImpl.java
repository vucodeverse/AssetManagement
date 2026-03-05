package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.model.warehouse.Zone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ZoneDAOImpl implements ZoneDAO {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<Zone> rowMapper = new RowMapper<Zone>() {
        @Override
        public Zone mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Zone.builder()
                    .id(rs.getInt("id"))
                    .warehouseId(rs.getInt("warehouse_id"))
                    .name(rs.getString("name"))
                    .assignedAssetTypeId(
                            rs.getObject("assigned_asset_type_id") != null ? rs.getInt("assigned_asset_type_id") : null)
                    .maxCapacity(rs.getInt("max_capacity"))
                    .currentCapacity(rs.getInt("current_capacity"))
                    .status(rs.getString("status"))
                    .build();
        }
    };

    @Override
    public List<Zone> findByWarehouseId(Integer warehouseId) {
        String sql = "SELECT * FROM wh_zone WHERE warehouse_id = ?";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }

    @Override
    public int insert(Zone zone) {
        String sql = "INSERT INTO wh_zone (warehouse_id, name, assigned_asset_type_id, max_capacity, current_capacity, status) VALUES (?, ?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, zone.getWarehouseId());
            ps.setString(2, zone.getName());
            if (zone.getAssignedAssetTypeId() != null) {
                ps.setInt(3, zone.getAssignedAssetTypeId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, zone.getMaxCapacity());
            ps.setInt(5, zone.getCurrentCapacity());
            ps.setString(6, zone.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            zone.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public int updateCapacity(Integer id, Integer currentCapacity) {
        String sql = "UPDATE wh_zone SET current_capacity = ? WHERE id = ?";
        return jdbcTemplate.update(sql, currentCapacity, id);
    }
}
