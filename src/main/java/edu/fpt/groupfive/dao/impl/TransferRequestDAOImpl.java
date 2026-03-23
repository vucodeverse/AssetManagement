package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.model.TransferRequest;
import edu.fpt.groupfive.dao.TransferRequestDAO;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransferRequestDAOImpl implements TransferRequestDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public int createTransferRequest(TransferRequest request) {

        String query = """
                    INSERT INTO transfer_request (
                        allocation_request_id, from_department_id, to_department_id,
                        asset_manager_id, transfer_date, reason, status,
                        sender_confirmed_by, sender_confirmed_at,
                        receiver_confirmed_by, receiver_confirmed_at,
                        created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, request.getAllocationRequestId());
            ps.setObject(2, request.getFromDepartmentId());
            ps.setObject(3, request.getToDepartmentId());
            ps.setObject(4, request.getAssetManagerId());
            ps.setTimestamp(5, Timestamp.valueOf(request.getTransferDate()));
            ps.setString(6, request.getReason());
            ps.setString(7, request.getStatus());
            ps.setObject(8, request.getSenderConfirmedBy());
            ps.setTimestamp(9, request.getSenderConfirmedAt() != null ? Timestamp.valueOf(request.getSenderConfirmedAt()) : null);
            ps.setObject(10, request.getReceiverConfirmedBy());
            ps.setTimestamp(11, request.getReceiverConfirmedAt() != null ? Timestamp.valueOf(request.getReceiverConfirmedAt()) : null);

            int rows = ps.executeUpdate();

            if (rows != 1) {
                throw new RuntimeException("Tạo yêu cầu điều chuyển thất bại");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new RuntimeException("Không lấy được transfer_id");

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo yêu cầu điều chuyển", e);
        }
    }

    @Override
    public int updateStatus(int transferId, String status) {
        String query = """
                    UPDATE transfer_request
                    SET status = ?, updated_at = SYSDATETIME()
                    WHERE transfer_id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setInt(2, transferId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật trạng thái yêu cầu điều chuyển", e);
        }
    }

    @Override
    public Optional<TransferRequest> findById(int transferId) {
        String query = """
                    SELECT transfer_id, allocation_request_id, from_department_id, to_department_id,
                           asset_manager_id, transfer_date, reason, status,
                           sender_confirmed_by, sender_confirmed_at,
                           receiver_confirmed_by, receiver_confirmed_at,
                           created_at, updated_at
                    FROM transfer_request
                    WHERE transfer_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm yêu cầu điều chuyển theo ID", e);
        }
    }

    @Override
    public List<TransferRequest> findAll() {
        String query = """
                    SELECT transfer_id, allocation_request_id, from_department_id, to_department_id,
                           asset_manager_id, transfer_date, reason, status,
                           sender_confirmed_by, sender_confirmed_at,
                           receiver_confirmed_by, receiver_confirmed_at,
                           created_at, updated_at
                    FROM transfer_request
                """;

        List<TransferRequest> requests = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requests.add(mapRow(rs));
            }
            return requests;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách yêu cầu điều chuyển", e);
        }
    }

    @Override
    public void delete(int transferRequestId) {
        String query = "DELETE FROM transfer_request WHERE transfer_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, transferRequestId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa yêu cầu điều chuyển. Id: " + transferRequestId, e);
        }
    }

    @Override
    public void updateSenderConfirm(int transferId, int userId, LocalDateTime time) {
        String query = """
                    UPDATE transfer_request
                    SET sender_confirmed_by = ?, 
                        sender_confirmed_at = ?, 
                        updated_at = SYSDATETIME()
                    WHERE transfer_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(time));
            ps.setInt(3, transferId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xác nhận bàn giao tài sản", e);
        }
    }

    @Override
    public void updateReceiverConfirm(int transferId, int userId, LocalDateTime time) {
        String query = """
                    UPDATE transfer_request
                    SET receiver_confirmed_by = ?, 
                        receiver_confirmed_at = ?, 
                        updated_at = SYSDATETIME()
                    WHERE transfer_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(time));
            ps.setInt(3, transferId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xác nhận nhận tài sản", e);
        }
    }

    private TransferRequest mapRow(ResultSet rs) throws SQLException {
        TransferRequest request = new TransferRequest();
        request.setTransferId(rs.getInt("transfer_id"));
        request.setAllocationRequestId((Integer) rs.getObject("allocation_request_id"));
        request.setFromDepartmentId((Integer) rs.getObject("from_department_id"));
        request.setToDepartmentId((Integer) rs.getObject("to_department_id"));
        request.setAssetManagerId((Integer) rs.getObject("asset_manager_id"));
        request.setTransferDate(rs.getTimestamp("transfer_date").toLocalDateTime());
        request.setReason(rs.getString("reason"));
        request.setStatus(rs.getString("status"));
        request.setSenderConfirmedBy((Integer) rs.getObject("sender_confirmed_by"));
        Timestamp senderAt = rs.getTimestamp("sender_confirmed_at");
        request.setSenderConfirmedAt(senderAt != null ? senderAt.toLocalDateTime() : null);
        request.setReceiverConfirmedBy((Integer) rs.getObject("receiver_confirmed_by"));
        Timestamp receiverAt = rs.getTimestamp("receiver_confirmed_at");
        request.setReceiverConfirmedAt(receiverAt != null ? receiverAt.toLocalDateTime() : null);
        request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        request.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return request;
    }


    @Override
    public List<TransferRequest> findByFromDepartmentId(Integer fromDeptId) {
        String sql = "SELECT * FROM transfer_request WHERE from_department_id = ? ORDER BY created_at DESC";
        List<TransferRequest> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fromDeptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<TransferRequest> findByToDepartmentId(Integer toDeptId) {
        String sql = "SELECT * FROM transfer_request WHERE to_department_id = ? ORDER BY created_at DESC";
        List<TransferRequest> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, toDeptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<TransferRequest> findByStatus(String status) {
        String sql = "SELECT * FROM transfer_request WHERE status = ? ORDER BY created_at DESC";
        List<TransferRequest> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    //pagination

    private void appendFilters(StringBuilder sql, List<Object> params, TransferSearchCriteria criteria) {
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            sql.append(" AND status = ?");
            params.add(criteria.getStatus());
        }
        if (criteria.getFromDate() != null) {
            sql.append(" AND created_at >= ?");
            params.add(criteria.getFromDate().atStartOfDay());
        }
        if (criteria.getToDate() != null) {
            sql.append(" AND created_at <= ?");
            params.add(criteria.getToDate().atTime(23,59,59));
        }
    }

    private String getOrderByColumn(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "created_at";
        }
        switch (sortField) {
            case "transferId": return "transfer_id";
            case "status": return "status";
            case "createdAt": return "created_at";
            default: return "created_at";
        }
    }

    private List<TransferRequest> executeQuery(String sql, List<Object> params) {
        List<TransferRequest> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thực thi truy vấn: " + sql, e);
        }
        return list;
    }

    private int executeCount(String sql, List<Object> params) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đếm số lượng: " + sql, e);
        }
    }

    @Override
    public List<TransferRequest> search(TransferSearchCriteria criteria, int offset, int size, String sortField, String sortDir) {
        StringBuilder sql = new StringBuilder("SELECT * FROM transfer_request WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, criteria);

        String orderBy = getOrderByColumn(sortField);
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(direction)
                .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(size);

        return executeQuery(sql.toString(), params);
    }

    @Override
    public int countSearch(TransferSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transfer_request WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, criteria);
        return executeCount(sql.toString(), params);
    }

    @Override
    public List<TransferRequest> searchForDepartmentManager(int departmentId, TransferSearchCriteria criteria, int offset, int size, String sortField, String sortDir) {
        StringBuilder sql = new StringBuilder("SELECT * FROM transfer_request WHERE (from_department_id = ? OR to_department_id = ?)");
        List<Object> params = new ArrayList<>();
        params.add(departmentId);
        params.add(departmentId);
        appendFilters(sql, params, criteria);

        String orderBy = getOrderByColumn(sortField);
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(direction)
                .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(size);

        return executeQuery(sql.toString(), params);
    }

    @Override
    public int countForDepartmentManager(int departmentId, TransferSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transfer_request WHERE (from_department_id = ? OR to_department_id = ?)");
        List<Object> params = new ArrayList<>();
        params.add(departmentId);
        params.add(departmentId);
        appendFilters(sql, params, criteria);
        return executeCount(sql.toString(), params);
    }
}