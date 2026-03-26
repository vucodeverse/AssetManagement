package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhAssetCapacityDAO;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WhAssetCapacityDAOImpl implements WhAssetCapacityDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AssetCapacity> rowMapper = (rs, rowNum) -> new AssetCapacity(
            rs.getInt("asset_type_id"),
            rs.getInt("unit_volume")
    );

    @Override
    @SuppressWarnings("null")
    public List<AssetCapacity> findAll() {
        String sql = "SELECT asset_type_id, unit_volume FROM wh_asset_capacity";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<AssetCapacity> findByAssetTypeId(int assetTypeId) {
        String sql = "SELECT asset_type_id, unit_volume FROM wh_asset_capacity WHERE asset_type_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, assetTypeId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveOrUpdate(int assetTypeId, int unitVolume) {
        String sql = "IF EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = ?) " +
                     "BEGIN " +
                     "    UPDATE wh_asset_capacity SET unit_volume = ? WHERE asset_type_id = ?; " +
                     "END " +
                     "ELSE " +
                     "BEGIN " +
                     "    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (?, ?); " +
                     "END";
        jdbcTemplate.update(sql, assetTypeId, unitVolume, assetTypeId, assetTypeId, unitVolume);
    }
}
