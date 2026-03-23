package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.model.AssetHandoverDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetHandoverDetailDaoImpl implements AssetHandoverDetailDao {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insertBatch(Integer handoverId, List<AssetHandoverDetail> details) {
        String query = """
                INSERT INTO asset_handover_detail
                (handover_id, asset_id, note)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            for (AssetHandoverDetail detail : details) {
                // ID lấy từ bảng cha vừa insert
                ps.setInt(1, handoverId);
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

    @Override
    public void deleteByHandoverId(Integer handoverId) {
        String query = """
                DELETE FROM asset_handover_detail WHERE handover_id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, handoverId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
