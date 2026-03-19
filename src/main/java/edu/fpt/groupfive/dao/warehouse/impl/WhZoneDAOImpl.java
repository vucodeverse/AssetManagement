package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhZoneDAO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
        
        // Cần join thêm asset_type để lấy tên loại tài sản nếu cần, hoặc xử lý ở service
        // Hiện tại DTO này đang dùng cho list zone tổng quát
        return dto;
    };

    @Override
    @SuppressWarnings("null")
    public List<ZoneCapacityResponseDTO> getAllZonesWithCapacity() {
        String sql = "SELECT * FROM wh_zones WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(sql, zoneMapper);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<ZoneCapacityResponseDTO> getZoneById(int zoneId) {
        String sql = "SELECT * FROM wh_zones WHERE zone_id = ?";
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
}
