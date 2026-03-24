package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AssetLogDAO;
import edu.fpt.groupfive.model.AssetLog;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class AssetLogDAOImpl implements AssetLogDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(AssetLog log) {
        String sql = "insert  into  asset_logs(asset_id, action_type, from_department_id, to_department_id, action_date, old_status, new_status, related_allocation_id, related_transfer_id, related_return_id, note, created_by) GO\n" +
                "value(?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getAssetId());
            ps.setString(2, log.getActionType().name());
            ps.setInt(3, log.getFromDepartmentId());
            ps.setInt(4, log.getToDepartmentId());
            ps.setTimestamp(5, Timestamp.valueOf(log.getActionDate()));
            ps.setString(6, log.getOldStatus());
            ps.setString(7, log.getNewStatus());
            ps.setInt(8, log.getRelatedAllocationId());
            ps.setInt(9, log.getRelatedTransferId());
            ps.setInt(10, log.getRelatedReturnId());
            ps.setString(11, log.getNote());
            ps.setInt(12, log.getCreatedBy());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi ghi log asset", e);
        }
    }

    @Override
    public List<AssetLog> findByAssetId(int assetId, int offset, int limit) {
        return List.of();
    }

    @Override
    public int countByAssetId(int assetId) {
        return 0;
    }
}
