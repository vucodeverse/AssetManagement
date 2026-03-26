package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO.HandoverItemDTO;
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
    public void insert(AssetHandoverDetail detail) {
        String query = """
                INSERT INTO asset_handover_detail
                (handover_id, asset_id, note)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, detail.getHandoverId());
            ps.setInt(2, detail.getAssetId());
            ps.setString(3, detail.getNote());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    @Override
    public List<HandoverItemDTO> findItemsByHandoverId(Integer handoverId) {
        String sql = """
                 SELECT 
                    ahd.asset_id,
                    CAST(a.asset_id AS NVARCHAR(50)) as asset_code,
                    at.type_name AS asset_type_name,
                    CASE 
                        WHEN EXISTS (
                            SELECT 1 
                            FROM map_handover_transactions mht
                            JOIN wh_transactions wt ON mht.transaction_id = wt.transaction_id
                            WHERE mht.asset_handover_id = ahd.handover_id 
                              AND wt.asset_id = ahd.asset_id 
                              AND wt.transaction_type = 'INBOUND'
                        ) THEN 1 ELSE 0 
                    END as is_scanned
                 FROM asset_handover_detail ahd
                 JOIN asset a ON ahd.asset_id = a.asset_id
                 JOIN asset_type at ON a.asset_type_id = at.asset_type_id
                 WHERE ahd.handover_id = ?
                 """;
        List<HandoverItemDTO> list = new ArrayList<>();

        try (Connection con = databaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, handoverId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(HandoverItemDTO.builder()
                        .assetId(rs.getInt("asset_id"))
                        .assetCode(rs.getString("asset_code"))
                        .assetTypeName(rs.getString("asset_type_name"))
                        .isScanned(rs.getInt("is_scanned") == 1)
                        .build());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
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
