package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AllocationReqDetailDaoImpl implements AllocationReqDetailDao {

    private final DatabaseConfig databaseConfig;


    @Override
    public void insertBatch(Integer requestId, List<AllocationRequestDetail> details) {
        String query = """
                INSERT INTO allocation_request_detail
                (request_id, asset_type_id,
                 quantity_requested, note)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            for (AllocationRequestDetail detail : details) {
                // ID lấy từ bảng cha vừa insert
                ps.setInt(1, requestId);
                ps.setInt(2, detail.getAssetTypeId());
                ps.setInt(3, detail.getRequestedQuantity());
                ps.setString(4, detail.getNote());

                // Thêm vào hàng đợi xử lý
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
