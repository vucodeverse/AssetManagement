package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.AssetHandoverDao;
import edu.fpt.groupfive.model.AssetHandover;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class AssetHandoverDaoImpl implements AssetHandoverDao {

    private final DatabaseConfig databaseConfig;

    private AssetHandover mapRowToHandover(ResultSet rs) throws Exception {
        AssetHandover handover = new AssetHandover();
        handover.setHandoverId(rs.getInt("handover_id"));
        handover.setHandoverType(rs.getString("handover_type"));
        int allocId = rs.getInt("allocation_request_id");
        handover.setAllocationRequestId(rs.wasNull() ? null : allocId);
        int returnId = rs.getInt("return_request_id");
        handover.setReturnRequestId(rs.wasNull() ? null : returnId);
        int fromDeptId = rs.getInt("from_department_id");
        handover.setFromDepartmentId(rs.wasNull() ? null : fromDeptId);
        int toDeptId = rs.getInt("to_department_id");
        handover.setToDepartmentId(rs.wasNull() ? null : toDeptId);
        handover.setStatus(Status.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            handover.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            handover.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return handover;
    }

    @Override
    public Integer insert(AssetHandover a) {

        String sql = """
                INSERT INTO asset_handover
                (handover_type, allocation_request_id, return_request_id,
                 from_department_id, to_department_id, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = databaseConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getHandoverType());

            if (a.getAllocationRequestId() != null)
                ps.setInt(2, a.getAllocationRequestId());
            else
                ps.setNull(2, Types.INTEGER);

            if (a.getReturnRequestId() != null)
                ps.setInt(3, a.getReturnRequestId());
            else
                ps.setNull(3, Types.INTEGER);

            if (a.getFromDepartmentId() != null)
                ps.setInt(4, a.getFromDepartmentId());
            else
                ps.setNull(4, Types.INTEGER);

            if (a.getToDepartmentId() != null)
                ps.setInt(5, a.getToDepartmentId());
            else
                ps.setNull(5, Types.INTEGER);

            ps.setString(6, a.getStatus().name());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AssetHandover> findAllByAllocationRequest() {
        String query = """
                SELECT * FROM asset_handover
                        WHERE handover_type = 'ALLOCATION'
                            OR allocation_request_id IS NOT NULL
                """;

        List<AssetHandover> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowToHandover(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public List<AssetHandover> findAllByReturnRequest() {
        String query = """
                SELECT * FROM asset_handover
                         WHERE handover_type = 'RETURN'
                            OR return_request_id IS NOT NULL
                """;

        List<AssetHandover> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowToHandover(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
