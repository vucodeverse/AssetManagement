package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.ReturnReqDAO;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.model.ReturnRequest;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReturnReqDAOImpl implements ReturnReqDAO {

    private final DatabaseConfig databaseConfig;

    private ReturnRequest mapRowToRequest(ResultSet rs) throws Exception {
        ReturnRequest request = new ReturnRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setRequesterId(rs.getInt("requester_id"));
        request.setRequestedDepartmentId(rs.getInt("requested_department_id"));

        request.setRequestReason(rs.getString("reason"));
        request.setStatus(rs.getString("status"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null)
            request.setCreatedAt(created.toLocalDateTime());
        if (updated != null)
            request.setUpdateAt(updated.toLocalDateTime());

        int approvedBy = rs.getInt("wh_confirmed_by");
        if (!rs.wasNull()) {
            request.setWhApprovedByUserId(approvedBy);
        }

        Timestamp approvedAt = rs.getTimestamp("wh_confirmed_at");
        if (approvedAt != null) {
            request.setWhApprovedDate(approvedAt.toLocalDateTime());
        }


        return request;
    }

    @Override
    public List<ReturnRequest> findAll(Integer departmentId) {
        String query = """
                SELECT * FROM return_request
                WHERE requested_department_id = ?
                         ORDER BY request_id ASC
                """;

        List<ReturnRequest> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, departmentId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowToRequest(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public ReturnRequest findById(Integer id) {
        String query = """
                SELECT * FROM return_request
                WHERE request_id = ?
                """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToRequest(rs);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Integer insert(ReturnRequest request) {
        String query = """
                INSERT INTO return_request
                (requester_id, requested_department_id,
                 request_date, reason, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, request.getRequesterId());
            ps.setInt(2, request.getRequestedDepartmentId());
            ps.setTimestamp(3, Timestamp.valueOf(request.getRequestDate()));
            ps.setString(4, request.getRequestReason());
            ps.setString(5, request.getStatus());

            ps.executeUpdate();

            // Lấy ID vừa tạo để trả về cho Service lưu bảng Detail
            try (var generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Lấy ID thành công
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Không lấy được Request ID");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ReturnRequest request) {
        String query = """
                UPDATE return_request
                SET
                    reason = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE request_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, request.getRequestReason());
            ps.setInt(2, request.getRequestId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer requestId) {
        String query = """
                DELETE FROM return_request
                WHERE request_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, requestId);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReturnRequest> search(Integer departmentId, String requestId, LocalDate fromDate, LocalDate toDate) {
        return List.of();
    }
}
