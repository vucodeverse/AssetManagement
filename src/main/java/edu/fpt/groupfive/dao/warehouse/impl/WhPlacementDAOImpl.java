package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhPlacementDAO;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class WhPlacementDAOImpl implements WhPlacementDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void placeAsset(Integer assetId, Integer zoneId, Integer userId) {
        try (Connection conn = databaseConfig.getConnection()) {
            placeAsset(assetId, zoneId, userId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to place asset", e);
        }
    }

    @Override
    public void placeAsset(Integer assetId, Integer zoneId, Integer userId, Connection conn) {
        // Since asset_id is PRIMARY KEY in wh_asset_placement, we use UPSERT pattern if needed, 
        // but for inbound it should be a pure insert.
        String sql = "INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ps.setInt(2, zoneId);
            ps.setInt(3, userId);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            // Check if it already exists, if so update
            String updateSql = "UPDATE wh_asset_placement SET zone_id = ?, placed_by = ?, placed_at = ? WHERE asset_id = ?";
            try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                ups.setInt(1, zoneId);
                ups.setInt(2, userId);
                ups.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ups.setInt(4, assetId);
                ups.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to place/update asset location", ex);
            }
        }
    }
}
