package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.model.AssetHandoverDetail;
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
                ps.setInt(1, handoverId);
                ps.setInt(2, detail.getAssetId());
                ps.setString(3, detail.getNote());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AssetHandoverDetail> findAllByHandoverId(Integer handoverId) {
        String sql = "SELECT handover_detail_id, handover_id, asset_id, note FROM asset_handover_detail WHERE handover_id = ?";
        List<AssetHandoverDetail> list = new ArrayList<>();

        try (Connection con = databaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, handoverId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AssetHandoverDetail d = new AssetHandoverDetail();
                d.setHandoverDetailId(rs.getInt("handover_detail_id"));
                d.setHandoverId(rs.getInt("handover_id"));
                d.setAssetId(rs.getInt("asset_id"));
                d.setNote(rs.getString("note"));
                list.add(d);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
