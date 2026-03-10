package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AllocationReqDao;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AllocationReqDaoImpl implements AllocationReqDao {

    private final DatabaseConfig databaseConfig;

    private AllocationRequest mapRowToRequest(ResultSet rs) throws Exception {
        AllocationRequest request = new AllocationRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setRequesterId(rs.getInt("requester_id"));
        request.setRequestedDepartmentId(rs.getInt("requested_department_id"));
        Date neededDate = rs.getDate("needed_by_date");
        if (neededDate != null)
            request.setNeededByDate(neededDate.toLocalDate());
        request.setPriority(rs.getString("priority"));
        request.setRequestReason(rs.getString("reason"));
        request.setStatus(rs.getString("status"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null)
            request.setCreatedAt(created.toLocalDateTime());
        if (updated != null)
            request.setUpdateAt(updated.toLocalDateTime());

        int amApprovedBy = rs.getInt("am_approved_by");
        if (!rs.wasNull()) {
            request.setAssetManagerApprovedByUserId(amApprovedBy);
        }

        Timestamp amApprovedAt = rs.getTimestamp("am_approved_at");
        if (amApprovedAt != null) {
            request.setAssetManagerApprovedDate(amApprovedAt.toLocalDateTime());
        }

        request.setRejectReason(rs.getString("reason_reject"));

        return request;
    }

    @Override
    public List<AllocationRequest> findAll(Integer departmentId) {
        String query = """
                SELECT * FROM allocation_request
                         WHERE requested_department_id = ?
                         ORDER BY request_id ASC
                """;

        List<AllocationRequest> list = new ArrayList<>();

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
    public AllocationRequest findById(Integer id) {
        String query = """
                SELECT * FROM allocation_request
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
    public Integer insert(AllocationRequest request) {
        String query = """
                INSERT INTO allocation_request
                (requester_id, requested_department_id,
                 request_date, needed_by_date,
                 priority, reason, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getRequesterId());
            ps.setInt(2, request.getRequestedDepartmentId());
            ps.setTimestamp(3, Timestamp.valueOf(request.getRequestDate()));
            ps.setObject(4, request.getNeededByDate());
            ps.setString(5, request.getPriority());
            ps.setString(6, request.getRequestReason());
            ps.setString(7, request.getStatus());

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
    public void update(AllocationRequest request) {
        String query = """
                UPDATE allocation_request
                SET needed_by_date = ?,
                    priority = ?,
                    reason = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE request_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setObject(1, request.getNeededByDate());
            ps.setString(2, request.getPriority());
            ps.setString(3, request.getRequestReason());
            ps.setInt(4, request.getRequestId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String query = """
                DELETE FROM allocation_request WHERE request_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(Integer id, String status, Integer amApprovedBy, String reasonReject) {
        String query = """
                UPDATE allocation_request
                SET status = ?,
                    am_approved_by = ?,
                    am_approved_at = CURRENT_TIMESTAMP,
                    reason_reject = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE request_id = ?
                """;

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setInt(2, amApprovedBy);
            ps.setString(3, reasonReject);
            ps.setInt(4, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AllocationRequest> search(Integer departmentId, String requestId, String status,
            String priority, LocalDate fromDate, LocalDate toDate
    /* int offset, int size */) {
        StringBuilder query = new StringBuilder("""
                    SELECT *
                    FROM allocation_request
                    WHERE requested_department_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(departmentId);

        if (requestId != null && !requestId.trim().isEmpty()) {
            query.append(" AND request_id = ?");
            params.add(Integer.parseInt(requestId.trim()));
        }

        if (status != null && !status.isEmpty()) {
            query.append(" AND status = ?");
            params.add(status);
        }

        if (priority != null && !priority.isEmpty()) {
            query.append(" AND priority = ?");
            params.add(priority);
        }

        if (fromDate != null) {
            query.append(" AND request_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            query.append(" AND request_date <= ?");
            params.add(toDate);
        }

        // query.append("""
        // ORDER BY request_id ASC
        // OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        // """);
        //
        // params.add(offset);
        // params.add(size);

        List<AllocationRequest> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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
    public int countInFilter(Integer departmentId, String requestId, String status,
            String priority, LocalDate fromDate, LocalDate toDate) {
        StringBuilder query = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM allocation_request
                    WHERE requested_department_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(departmentId);

        if (requestId != null && !requestId.trim().isEmpty()) {
            query.append(" AND request_id = ?");
            params.add(Integer.parseInt(requestId.trim()));
        }

        if (status != null && !status.isEmpty()) {
            query.append(" AND status = ?");
            params.add(status);
        }

        if (priority != null && !priority.isEmpty()) {
            query.append(" AND priority = ?");
            params.add(priority);
        }

        if (fromDate != null) {
            query.append(" AND request_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            query.append(" AND request_date <= ?");
            params.add(toDate);
        }

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return 0;

    }

    @Override
    public int countAll(Integer departmentId) {
        String query = """
                SELECT COUNT(*) FROM allocation_request
                WHERE requested_department_id = ?
                """;

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, departmentId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
