package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.ReturnReqDetailDAO;
import edu.fpt.groupfive.model.ReturnRequestDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReturnReqDetailDAOImpl implements ReturnReqDetailDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insertBatch(Integer requestId, List<ReturnRequestDetail> details) {
        String query = """
                INSERT INTO return_request_detail
                (request_id, asset_id, note)
                VALUES (?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            for (ReturnRequestDetail detail : details) {
                // ID lấy từ bảng cha vừa insert
                ps.setInt(1, requestId);
                ps.setInt(2, detail.getAssetId());
                ps.setString(3, detail.getNote());

                // Thêm vào hàng đợi xử lý
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
