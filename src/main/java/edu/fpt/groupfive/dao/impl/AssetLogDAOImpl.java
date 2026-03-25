package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.AssetActionType;
import edu.fpt.groupfive.dao.AssetLogDAO;
import edu.fpt.groupfive.model.AssetLog;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetLogDAOImpl implements AssetLogDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(AssetLog log) {
        String sql = "INSERT INTO asset_logs (asset_id, action_type, from_department_id, to_department_id, " +
                "action_date, old_status, new_status, related_allocation_id, related_transfer_id, " +
                "related_return_id, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getAssetId());
            ps.setString(2, log.getActionType().name());
            setInt(ps, 3, log.getFromDepartmentId());
            setInt(ps, 4, log.getToDepartmentId());
            ps.setTimestamp(5, Timestamp.valueOf(log.getActionDate()));
            ps.setString(6, log.getOldStatus());
            ps.setString(7, log.getNewStatus());
            setInt(ps, 8, log.getRelatedAllocationId());
            setInt(ps, 9, log.getRelatedTransferId());
            setInt(ps, 10, log.getRelatedReturnId());
            ps.setString(11, log.getNote());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi ghi log asset", e);
        }
    }

    @Override
    public List<AssetLog> findByAssetId(int assetId) {
        String sql = "SELECT * FROM asset_logs WHERE asset_id = ? ORDER BY action_date ASC";

        List<AssetLog> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AssetLog log = new AssetLog();
                log.setAssetLogId(rs.getInt("asset_log_id"));
                log.setAssetId(rs.getInt("asset_id"));
                log.setActionType(AssetActionType.valueOf(rs.getString("action_type")));
                log.setFromDepartmentId((Integer) rs.getObject("from_department_id"));
                log.setToDepartmentId((Integer) rs.getObject("to_department_id"));
                Timestamp ts = rs.getTimestamp("action_date");
                log.setActionDate(ts != null ? ts.toLocalDateTime() : null);
                log.setOldStatus(rs.getString("old_status"));
                log.setNewStatus(rs.getString("new_status"));
                log.setRelatedAllocationId((Integer) rs.getObject("related_allocation_id"));
                log.setRelatedTransferId((Integer) rs.getObject("related_transfer_id"));
                log.setRelatedReturnId((Integer) rs.getObject("related_return_id"));
                log.setNote(rs.getString("note"));
                list.add(log);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi ghi log asset", e);
        }
    }

    private void setInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }
}