package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhZoneDAO;
import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WhZoneDAOImpl implements WhZoneDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ZoneCapacityResponseDTO> zoneMapper = (rs, rowNum) -> {
        ZoneCapacityResponseDTO dto = new ZoneCapacityResponseDTO();
        dto.setZoneId(rs.getInt("zone_id"));
        dto.setWarehouseId(rs.getInt("warehouse_id"));
        dto.setZoneName(rs.getString("zone_name"));
        dto.setMaxCapacity(rs.getInt("max_capacity"));
        dto.setCurrentCapacity(rs.getInt("current_capacity"));
        dto.setAssetTypeId(rs.getObject("asset_type_id", Integer.class));
        dto.setStatus(rs.getString("status"));
        
        dto.setAssetTypeName(rs.getString("asset_type_name"));
        
        return dto;
    };

    @Override
    @SuppressWarnings("null")
    public List<ZoneCapacityResponseDTO> getAllZonesWithCapacity() {
        String sql = "SELECT z.*, t.type_name as asset_type_name FROM wh_zones z LEFT JOIN asset_type t ON z.asset_type_id = t.asset_type_id WHERE z.status = 'ACTIVE'";
        return jdbcTemplate.query(sql, zoneMapper);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<ZoneCapacityResponseDTO> getZoneById(int zoneId) {
        String sql = "SELECT z.*, t.type_name as asset_type_name FROM wh_zones z LEFT JOIN asset_type t ON z.asset_type_id = t.asset_type_id WHERE z.zone_id = ?";
        List<ZoneCapacityResponseDTO> results = jdbcTemplate.query(sql, zoneMapper, zoneId);
        return results.stream().findFirst();
    }

    @Override
    public void updateZone(int zoneId, String zoneName, int maxCapacity) {
        String sql = "UPDATE wh_zones SET zone_name = ?, max_capacity = ? WHERE zone_id = ?";
        jdbcTemplate.update(sql, zoneName, maxCapacity, zoneId);
    }

    @Override
    public void createZone(WarehouseZone zone) {
        String sql = "INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, zone.getWarehouseId(), zone.getZoneName(), zone.getMaxCapacity(), zone.getStatus());
    }

    @Override
    public void updateCurrentCapacity(int assetTypeId, int unitVolume) {
        String sql = "UPDATE wh_zones " +
                     "SET current_capacity = (" +
                     "    SELECT COUNT(*) FROM wh_asset_placement ap " +
                     "    WHERE ap.zone_id = wh_zones.zone_id" +
                     ") * ? " +
                     "WHERE asset_type_id = ?";
        jdbcTemplate.update(sql, unitVolume, assetTypeId);
    }

    @Override
    public void updateCurrentCapacityForDecrease(int zoneId, int unitVolume) {
        String sql = "UPDATE wh_zones SET current_capacity = current_capacity - ? WHERE zone_id = ?";
        jdbcTemplate.update(sql, unitVolume, zoneId);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<AssetLocationResponseDTO> getAssetLocation(int assetId) {
        String sql = "SELECT " +
                     "   a.asset_id, " +
                     "   a.asset_name, " +
                     "   a.current_status, " +
                     "   z.zone_id, " +
                     "   z.zone_name, " +
                     "   (u.first_name + ' ' + u.last_name) AS placed_by, " +
                     "   p.placed_at " +
                     "FROM asset a " +
                     "LEFT JOIN wh_asset_placement p ON a.asset_id = p.asset_id " +
                     "LEFT JOIN wh_zones z ON p.zone_id = z.zone_id " +
                     "LEFT JOIN users u ON p.placed_by = u.user_id " +
                     "WHERE a.asset_id = ?";
        
        List<AssetLocationResponseDTO> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AssetLocationResponseDTO dto = AssetLocationResponseDTO.builder()
                .assetId(rs.getInt("asset_id"))
                .assetCode(String.valueOf(rs.getInt("asset_id")))
                .assetName(rs.getString("asset_name"))
                .status(rs.getString("current_status"))
                .zoneId(rs.getObject("zone_id", Integer.class))
                .zoneName(rs.getString("zone_name"))
                .placedBy(rs.getString("placed_by"))
                .build();
            
            Timestamp ts = rs.getTimestamp("placed_at");
            if (ts != null) {
                dto.setPlacedAt(ts.toLocalDateTime());
            }
            return dto;
        }, assetId);
        
        return results.stream().findFirst();
    }
}
