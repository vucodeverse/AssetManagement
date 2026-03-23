package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

    @Override
    public List<AllocationRequestDetail> findByRequestId(Integer requestId) {
        String query = """
                SELECT * FROM allocation_request_detail WHERE request_id = ?
                """;

        List<AllocationRequestDetail> details = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AllocationRequestDetail detail = new AllocationRequestDetail();
                detail.setRequestDetailId(rs.getInt("request_detail_id"));
                detail.setRequestId(rs.getInt("request_id"));
                detail.setAssetTypeId(rs.getInt("asset_type_id"));
                detail.setRequestedQuantity(rs.getInt("quantity_requested"));
                detail.setNote(rs.getString("note"));
                details.add(detail);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return details;
    }

    @Override
    public void deleteByRequestId(Integer requestId) {
        String query = """
                DELETE FROM allocation_request_detail WHERE request_id = ?
                """;

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, requestId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AllocationRequestDetail> findAllByHandoverId(Integer handoverId) {
        String query = """
                SELECT ard.*
                FROM allocation_request_detail ard
                    JOIN asset_handover ah ON ard.request_id = ah.allocation_request_id
                WHERE ah.handover_id = ?
                """;

        List<AllocationRequestDetail> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, handoverId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AllocationRequestDetail detail = new AllocationRequestDetail();
                    detail.setRequestDetailId(rs.getInt("request_detail_id"));
                    detail.setRequestId(rs.getInt("request_id"));
                    detail.setAssetTypeId(rs.getInt("asset_type_id"));
                    detail.setRequestedQuantity(rs.getInt("quantity_requested"));
                    detail.setNote(rs.getString("note"));

                    list.add(detail);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
