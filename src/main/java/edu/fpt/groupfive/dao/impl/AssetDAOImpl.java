package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
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
                    (asset_name,
                     serial_number,
                     current_status,
                     warranty_start_date,
                     warranty_end_date,
                     original_cost,
                     asset_type_id,
                     acquisition_date)
                    VALUES (?,?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, asset.getAssetName());
            ps.setString(2, asset.getSerialNumber());
            ps.setString(3, asset.getCurrentStatus().name());

            setDate(ps, 4, asset.getWarrantyStartDate());
            setDate(ps, 5, asset.getWarrantyEndDate());

            setBigDecimal(ps, 6, asset.getOriginalCost());

            ps.setInt(7, asset.getAssetTypeId());

            setDate(ps, 8, asset.getAcquisitionDate());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi không tạo được tài sản.", e);
        }
    }

    @Override
    public void update(Asset asset) {

        String sql = """
                    UPDATE asset
                    SET asset_name = ?,
                        serial_number = ?,
                        warranty_start_date = ?,
                        warranty_end_date = ?,
                        original_cost = ?,
                        asset_type_id = ?,
                        current_status=?,
                        acquisition_date = ?
                    WHERE asset_id = ?
                """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, asset.getAssetName());
            ps.setString(2, asset.getSerialNumber());
            setDate(ps, 3, asset.getWarrantyStartDate());
            setDate(ps, 4, asset.getWarrantyEndDate());
            setBigDecimal(ps, 5, asset.getOriginalCost());

            ps.setInt(6, asset.getAssetTypeId());
            ps.setString(7,asset.getCurrentStatus().name());
            setDate(ps, 8, asset.getAcquisitionDate());

            ps.setInt(9, asset.getAssetId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi không cập nhật được tài sản", e);
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
            throw new RuntimeException("Xóa tài sản thất bại", e);
        }
    }

    //  FIND BY ID
    @Override
    public Optional<Asset> findById(Integer id) {

        String sql = """
                    SELECT a.*,
                           t.type_name
                    FROM asset a
                    LEFT JOIN asset_type t
                        ON a.asset_type_id = t.asset_type_id
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
            throw new RuntimeException("Tìm tài sản với id = " + id + " thất bại.", e);
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
                        ON a.asset_type_id = t.asset_type_id
                """;

        List<Asset> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Tìm danh sách tài sản thất bại", e);
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
            throw new RuntimeException("Kiểm tra mã serial tài sản thất bại", e);
        }
    }

    @Override
    public Optional<AssetDetailResponse> findDetailById(Integer id) {

        String sql = """
    SELECT TOP 1
           a.asset_id,
           a.asset_name,
           a.serial_number,
           a.original_cost,

           a.acquisition_date,
           a.warranty_start_date,
           a.warranty_end_date,

           t.type_name,

           w.warehouse_name,
           r.rack_name,
           s.shelf_name,

           d.department_name,
           al.allocation_date

    FROM asset a
    LEFT JOIN asset_type t 
        ON a.asset_type_id = t.asset_type_id

    LEFT JOIN shelf s 
        ON a.shelf_id = s.shelf_id

    LEFT JOIN rack r 
        ON s.rack_id = r.rack_id

    LEFT JOIN warehouse w 
        ON r.warehouse_id = w.warehouse_id

    LEFT JOIN allocation_detail ad 
        ON a.asset_id = ad.asset_id

    LEFT JOIN allocation al 
        ON ad.allocation_id = al.allocation_id

    LEFT JOIN departments d 
        ON al.allocated_to_department_id = d.department_id

    WHERE a.asset_id = ?
    ORDER BY al.allocation_date DESC
""";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                   AssetDetailResponse dto=new AssetDetailResponse();
                    dto.setAssetId(rs.getInt("asset_id"));
                    dto.setAssetName(rs.getString("asset_name"));
                    dto.setSerialNumber(rs.getString("serial_number"));
                    dto.setOriginalCost(rs.getBigDecimal("original_cost"));

                    dto.setAssetTypeName(rs.getString("type_name"));
                    dto.setWarehouseName(rs.getString("warehouse_name"));
                    dto.setRackName(rs.getString("rack_name"));
                    dto.setShelfName(rs.getString("shelf_name"));
                    dto.setDepartmentName(rs.getString("department_name"));

                    dto.setAcquisitionDate(toLocalDate(rs.getDate("acquisition_date")));
                    dto.setWarrantyStartDate(toLocalDate(rs.getDate("warranty_start_date")));
                    dto.setWarrantyEndDate(toLocalDate(rs.getDate("warranty_end_date")));
                    Date allocationDate = rs.getDate("allocation_date");
                    if (allocationDate != null) {
                        dto.setAllocationDate(allocationDate.toLocalDate());
                    }
                    return Optional.of(dto);


                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi lấy chi tiết tài sản", e);
        }

        return Optional.empty();
    }





    private Asset mapResultSet(ResultSet rs) throws SQLException {

        Asset asset = new Asset();

        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setCurrentStatus(AssetStatus.valueOf(rs.getString("current_status").toUpperCase()));

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