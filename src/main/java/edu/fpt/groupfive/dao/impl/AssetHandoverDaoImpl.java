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
                SELECT handover_id, handover_type, allocation_request_id, return_request_id, from_department_id, to_department_id, status, created_at, updated_at 
                FROM asset_handover
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
                 SELECT handover_id, handover_type, allocation_request_id, return_request_id, from_department_id, to_department_id, status, created_at, updated_at 
                 FROM asset_handover
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

    @Override
    public List<AssetHandover> findAllProcessedReturns() {
        String query = """
                 SELECT 
                    h.*, 
                    fd.department_name as from_department_name,
                    td.department_name as to_department_name
                 FROM asset_handover h
                 LEFT JOIN departments fd ON h.from_department_id = fd.department_id
                 LEFT JOIN departments td ON h.to_department_id = td.department_id
                 WHERE h.handover_type = 'RETURN' AND h.status = 'APPROVED'
                 ORDER BY h.updated_at DESC
                 """;

        List<AssetHandover> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AssetHandover h = mapRowToHandover(rs);
                h.setFromDepartmentName(rs.getString("from_department_name"));
                h.setToDepartmentName(rs.getString("to_department_name"));
                list.add(h);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public List<AssetHandover> findAllPendingReturns() {
        String query = """
                 SELECT 
                    h.*, 
                    fd.department_name as from_department_name,
                    td.department_name as to_department_name
                 FROM asset_handover h
                 LEFT JOIN departments fd ON h.from_department_id = fd.department_id
                 LEFT JOIN departments td ON h.to_department_id = td.department_id
                 WHERE h.handover_type = 'RETURN' AND h.status = 'PENDING'
                 ORDER BY h.created_at DESC
                 """;

        List<AssetHandover> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AssetHandover h = mapRowToHandover(rs);
                // I need to set the department names in AssetHandover model
                h.setFromDepartmentName(rs.getString("from_department_name"));
                h.setToDepartmentName(rs.getString("to_department_name"));
                list.add(h);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public AssetHandover findById(Integer id) {
        String query = """
                 SELECT 
                    h.*, 
                    fd.department_name as from_department_name,
                    td.department_name as to_department_name
                 FROM asset_handover h
                 LEFT JOIN departments fd ON h.from_department_id = fd.department_id
                 LEFT JOIN departments td ON h.to_department_id = td.department_id
                 WHERE h.handover_id = ?
                 """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AssetHandover h = mapRowToHandover(rs);
                h.setFromDepartmentName(rs.getString("from_department_name"));
                h.setToDepartmentName(rs.getString("to_department_name"));
                return h;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void updateStatus(Integer id, Status status) {
        String query = "UPDATE asset_handover SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE handover_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Cập nhật trạng thái AssetHandover thất bại", e);
        }
    }
}
