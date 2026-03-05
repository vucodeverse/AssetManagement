package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AssetCapacityDAO;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AssetCapacityDAOImpl implements AssetCapacityDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<AssetCapacity> rowMapper = new RowMapper<AssetCapacity>() {
        @Override
        public AssetCapacity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AssetCapacity.builder()
                    .id(rs.getInt("id"))
                    .assetTypeId(rs.getInt("asset_type_id"))
                    .capacityUnits(rs.getInt("capacity_units"))
                    .build();
        }
    };

    @Override
    public int insert(AssetCapacity capacity) {
        String sql = "INSERT INTO wh_asset_capacity (asset_type_id, capacity_units) VALUES (?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, capacity.getAssetTypeId());
            ps.setInt(2, capacity.getCapacityUnits());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            capacity.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public int update(AssetCapacity capacity) {
        String sql = "UPDATE wh_asset_capacity SET capacity_units = ? WHERE asset_type_id = ?";
        return jdbcTemplate.update(sql, capacity.getCapacityUnits(), capacity.getAssetTypeId());
    }

    @Override
    public AssetCapacity findByAssetTypeId(Integer assetTypeId) {
        String sql = "SELECT * FROM wh_asset_capacity WHERE asset_type_id = ?";
        List<AssetCapacity> result = jdbcTemplate.query(sql, rowMapper, assetTypeId);
        return result.isEmpty() ? null : result.get(0);
    }
}
