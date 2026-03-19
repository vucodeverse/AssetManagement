package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AssetCapacityDAO;
import edu.fpt.groupfive.dto.warehouse.response.AssetCapacityResponse;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetCapacityDAOImpl implements AssetCapacityDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AssetCapacity> rowMapper = (rs, rowNum) -> new AssetCapacity(
            rs.getInt("asset_type_id"),
            rs.getInt("unit_volume")
    );

    @Override
    public void upsert(AssetCapacity capacity) {
        String sql = "IF EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = ?) " +
                     "UPDATE wh_asset_capacity SET unit_volume = ? WHERE asset_type_id = ? " +
                     "ELSE INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (?, ?)";
        jdbcTemplate.update(sql, capacity.getAssetTypeId(), capacity.getUnitVolume(), capacity.getAssetTypeId(),
                capacity.getAssetTypeId(), capacity.getUnitVolume());
    }

    @Override
    public Optional<AssetCapacity> findByAssetTypeId(Integer assetTypeId) {
        String sql = "SELECT * FROM wh_asset_capacity WHERE asset_type_id = ?";
        List<AssetCapacity> results = jdbcTemplate.query(sql, rowMapper, assetTypeId);
        return results.stream().findFirst();
    }

    @Override
    public List<AssetCapacityResponse> findAllWithAssetType() {
        String sql = """
                    SELECT t.asset_type_id, t.type_name, c.category_name, 
                           COALESCE(cap.unit_volume, 1) as unit_volume
                    FROM asset_type t
                    JOIN category c ON t.category_id = c.category_id
                    LEFT JOIN wh_asset_capacity cap ON t.asset_type_id = cap.asset_type_id
                    ORDER BY c.category_name, t.type_name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AssetCapacityResponse response = new AssetCapacityResponse();
            response.setAssetTypeId(rs.getInt("asset_type_id"));
            response.setTypeName(rs.getString("type_name"));
            response.setCategoryName(rs.getString("category_name"));
            response.setUnitVolume(rs.getInt("unit_volume"));
            return response;
        });
    }
}
