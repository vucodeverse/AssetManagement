package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.ReturnReqDAO;
import edu.fpt.groupfive.model.ReturnRequest;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class ReturnReqDAOImpl implements ReturnReqDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Integer insert(ReturnRequest request) {
        String query = """
                INSERT INTO allocation_request
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
}
