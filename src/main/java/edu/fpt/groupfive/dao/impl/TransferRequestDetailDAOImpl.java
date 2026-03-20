package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.TransferRequestDetailDAO;
import edu.fpt.groupfive.model.TransferRequestDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransferRequestDetailDAOImpl implements TransferRequestDetailDAO {

    private final DatabaseConfig databaseConfig;
    @Override
    public void batchInsertDetails(int transferId, List<Integer> assetIds) {

        String query = """
        INSERT INTO transfer_request_detail (transfer_id, asset_id)
        VALUES (?, ?)
    """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            for (Integer assetId : assetIds) {
                ps.setInt(1, transferId);
                ps.setInt(2, assetId);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi batch insert transfer request details", e);
        }
    }
    @Override
    public void createDetail(TransferRequestDetail detail) {
        String query = """
            INSERT INTO transfer_request_detail (
                transfer_id, allocation_request_detail_id, asset_id,
                condition_from_sender, note
            ) VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, detail.getTransferId());
            ps.setObject(2, detail.getAllocationRequestDetailId());
            ps.setInt(3, detail.getAssetId());
            ps.setString(4, detail.getConditionFromSender());
            ps.setString(5, detail.getNote());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Thêm chi tiết yêu cầu điều chuyển thất bại");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo chi tiết yêu cầu điều chuyển", e);
        }
    }

    @Override
    public int updateNote(int transferDetailId, String note) {
        String query = """
            UPDATE transfer_request_detail
            SET note = ?
            WHERE transfer_detail_id = ?
        """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, note);
            ps.setInt(2, transferDetailId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật ghi chú chi tiết điều chuyển", e);
        }
    }

    @Override
    public Optional<TransferRequestDetail> findById(int transferDetailId) {
        String query = """
            SELECT transfer_detail_id, transfer_id, allocation_request_detail_id,
                   asset_id, condition_from_sender, note
            FROM transfer_request_detail
            WHERE transfer_detail_id = ?
        """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, transferDetailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm chi tiết điều chuyển theo ID", e);
        }
    }

    @Override
    public List<TransferRequestDetail> findByTransferId(int transferId) {
        String query = """
            SELECT transfer_detail_id, transfer_id, allocation_request_detail_id,
                   asset_id, condition_from_sender, note
            FROM transfer_request_detail
            WHERE transfer_id = ?
        """;

        List<TransferRequestDetail> details = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapRow(rs));
                }
            }
            return details;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm danh sách chi tiết điều chuyển theo transfer_id", e);
        }
    }

    private TransferRequestDetail mapRow(ResultSet rs) throws SQLException {
        TransferRequestDetail detail = new TransferRequestDetail();
        detail.setTransferDetailId(rs.getInt("transfer_detail_id"));
        detail.setTransferId(rs.getInt("transfer_id"));
        detail.setAllocationRequestDetailId((Integer) rs.getObject("allocation_request_detail_id"));
        detail.setAssetId(rs.getInt("asset_id"));
        detail.setConditionFromSender(rs.getString("condition_from_sender"));
        detail.setNote(rs.getString("note"));
        return detail;
    }
}