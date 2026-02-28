package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetDAOImpl implements AssetDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(Asset asset) {

        String sql = """
            INSERT INTO asset
            (serial_number,
             current_status,
             warranty_start_date,
             warranty_end_date,
             original_cost,
             asset_type_id,
             acquisition_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, asset.getSerialNumber());
            ps.setString(2, asset.getCurrentStatus());

            setDate(ps, 3, asset.getWarrantyStartDate());
            setDate(ps, 4, asset.getWarrantyEndDate());

            setBigDecimal(ps, 5, asset.getOriginalCost());

            ps.setInt(6, asset.getAssetTypeId());

            setDate(ps, 7, asset.getAcquisitionDate());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Insert asset failed", e);
        }
    }

    @Override
    public void update(Asset asset) {

        String sql = """
            UPDATE asset
            SET serial_number = ?,
                current_status = ?,
                warranty_start_date = ?,
                warranty_end_date = ?,
                original_cost = ?,
                asset_type_id = ?,
                acquisition_date = ?
            WHERE asset_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, asset.getSerialNumber());
            ps.setString(2, asset.getCurrentStatus());

            setDate(ps, 3, asset.getWarrantyStartDate());
            setDate(ps, 4, asset.getWarrantyEndDate());
            setBigDecimal(ps, 5, asset.getOriginalCost());

            ps.setInt(6, asset.getAssetTypeId());
            setDate(ps, 7, asset.getAcquisitionDate());

            ps.setInt(8, asset.getAssetId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Update asset failed", e);
        }
    }


    @Override
    public void delete(Integer id) {

        String sql = "DELETE FROM asset WHERE asset_id = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Delete asset failed", e);
        }
    }

    // ================= FIND BY ID =================
    @Override
    public Optional<Asset> findById(Integer id) {

        String sql = """
        SELECT a.*,
               t.type_name
        FROM asset a
        LEFT JOIN asset_type t
            ON a.asset_type_id = t.type_id
        WHERE a.asset_id = ?
    """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Find by id failed", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Asset> findAll() {

        String sql = """
    SELECT a.*,
           t.type_name
    FROM asset a
    LEFT JOIN asset_type t
        ON a.asset_type_id = t.type_id
""";

        List<Asset> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Find all failed", e);
        }

        return list;
    }

    @Override
    public boolean existsBySerial(String serialNumber) {

        String sql = "SELECT 1 FROM asset WHERE serial_number = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, serialNumber);
            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException("Check serial failed", e);
        }
    }

    private Asset mapResultSet(ResultSet rs) throws SQLException {

        Asset asset = new Asset();

        asset.setAssetId(rs.getInt("asset_id"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setCurrentStatus(rs.getString("current_status"));

        asset.setOriginalCost(rs.getBigDecimal("original_cost"));
        asset.setAssetTypeId(rs.getInt("asset_type_id"));
        asset.setAssetTypeName(rs.getString("type_name"));
        asset.setWarrantyStartDate(toLocalDate(rs.getDate("warranty_start_date")));
        asset.setWarrantyEndDate(toLocalDate(rs.getDate("warranty_end_date")));
        asset.setAcquisitionDate(toLocalDate(rs.getDate("acquisition_date")));

        return asset;
    }

    private void setDate(PreparedStatement ps, int index, java.time.LocalDate date)
            throws SQLException {
        if (date != null) {
            ps.setDate(index, Date.valueOf(date));
        } else {
            ps.setNull(index, Types.DATE);
        }
    }

    private void setBigDecimal(PreparedStatement ps, int index, BigDecimal value)
            throws SQLException {
        if (value != null) {
            ps.setBigDecimal(index, value);
        } else {
            ps.setNull(index, Types.NUMERIC);
        }
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }
}