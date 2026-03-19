package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.WhZoneDAO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import edu.fpt.groupfive.model.warehouse.WarehouseZone;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WhZoneDAOImpl implements WhZoneDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public List<ZoneCapacityResponseDTO> getAllZonesWithCapacity() {
        String sql = """
                SELECT z.zone_id, z.zone_name, z.max_capacity, z.current_capacity, z.status,
                       a.asset_type_id, a.type_name as asset_type_name
                FROM wh_zones z
                LEFT JOIN asset_type a ON z.asset_type_id = a.asset_type_id
                ORDER BY z.zone_id
                """;

        List<ZoneCapacityResponseDTO> list = new ArrayList<>();
        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ZoneCapacityResponseDTO dto = new ZoneCapacityResponseDTO();
                dto.setZoneId(rs.getInt("zone_id"));
                dto.setZoneName(rs.getString("zone_name"));
                dto.setMaxCapacity(rs.getInt("max_capacity"));
                dto.setCurrentCapacity(rs.getInt("current_capacity"));
                dto.setStatus(rs.getString("status"));
                dto.setAssetTypeId(rs.getObject("asset_type_id", Integer.class));
                dto.setAssetTypeName(rs.getString("asset_type_name"));
                
                list.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<ZoneCapacityResponseDTO> getZoneById(int zoneId) {
        String sql = """
                SELECT z.zone_id, z.zone_name, z.max_capacity, z.current_capacity, z.status,
                       a.asset_type_id, a.type_name as asset_type_name
                FROM wh_zones z
                LEFT JOIN asset_type a ON z.asset_type_id = a.asset_type_id
                WHERE z.zone_id = ?
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, zoneId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ZoneCapacityResponseDTO dto = new ZoneCapacityResponseDTO();
                dto.setZoneId(rs.getInt("zone_id"));
                dto.setZoneName(rs.getString("zone_name"));
                dto.setMaxCapacity(rs.getInt("max_capacity"));
                dto.setCurrentCapacity(rs.getInt("current_capacity"));
                dto.setStatus(rs.getString("status"));
                dto.setAssetTypeId(rs.getObject("asset_type_id", Integer.class));
                dto.setAssetTypeName(rs.getString("asset_type_name"));
                
                return Optional.of(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void updateZone(int zoneId, String zoneName, int maxCapacity) {
        String sql = """
                UPDATE wh_zones
                SET zone_name = ?, max_capacity = ?
                WHERE zone_id = ?
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, zoneName);
            ps.setInt(2, maxCapacity);
            ps.setInt(3, zoneId);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createZone(WarehouseZone zone) {
        String sql = """
                INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, zone.getWarehouseId());
            ps.setString(2, zone.getZoneName());
            ps.setInt(3, zone.getMaxCapacity());
            ps.setInt(4, zone.getCurrentCapacity() != null ? zone.getCurrentCapacity() : 0);
            
            if (zone.getAssetTypeId() != null) {
                ps.setInt(5, zone.getAssetTypeId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            ps.setString(6, zone.getStatus() != null ? zone.getStatus() : "ACTIVE");

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
