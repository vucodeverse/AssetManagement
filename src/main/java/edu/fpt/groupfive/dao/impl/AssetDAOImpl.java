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
import java.time.LocalDate;
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
                     purchase_order_detail_id,
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
            ps.setInt(2, asset.getPurchaseOrderDetailId());
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
                        purchase_order_detail_id=?,
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
            ps.setInt(2, asset.getPurchaseOrderDetailId());

            setDate(ps, 3, asset.getWarrantyStartDate());
            setDate(ps, 4, asset.getWarrantyEndDate());
            setBigDecimal(ps, 5, asset.getOriginalCost());

            ps.setInt(6, asset.getAssetTypeId());
            ps.setString(7, asset.getCurrentStatus().name());
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

    // find asset by id
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
    public List<Asset> findAllByDepartmentId(Integer departmentId) {
        String sql = """
                SELECT a.*, t.type_name
                  FROM asset a
                        LEFT JOIN asset_type t
                                ON a.asset_type_id = t.asset_type_id
                        LEFT JOIN return_request_detail d
                                ON d.asset_id = a.asset_id
                        LEFT JOIN return_request r
                                ON r.request_id = d.request_id
                        AND r.status = 'PENDING_AM'
                WHERE a.department_id = ?
                        AND r.request_id IS NULL
                """;
        List<Asset> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private Asset mapResultSet2(ResultSet rs) throws SQLException {

        Asset asset = new Asset();

        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setPurchaseOrderDetailId(rs.getInt("purchase_order_detail_id"));
        asset.setCurrentStatus(AssetStatus.valueOf(rs.getString("current_status").toUpperCase()));

        asset.setOriginalCost(rs.getBigDecimal("original_cost"));
        asset.setAssetTypeId(rs.getInt("asset_type_id"));
        asset.setAssetTypeName(rs.getString("type_name"));
        asset.setWarrantyStartDate(toLocalDate(rs.getDate("warranty_start_date")));
        asset.setWarrantyEndDate(toLocalDate(rs.getDate("warranty_end_date")));
        asset.setAcquisitionDate(toLocalDate(rs.getDate("acquisition_date")));
        asset.setNote(rs.getString("note"));

        return asset;
    }

    @Override
    public List<Asset> findByReturnRequestId(Integer requestId) {
        String sql = """
                SELECT a.*, t.type_name, d.note
                FROM return_request_detail d
                JOIN asset a
                    ON a.asset_id = d.asset_id
                LEFT JOIN asset_type t
                    ON t.asset_type_id = a.asset_type_id
                WHERE d.request_id = ?
                """;

        List<Asset> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet2(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<AssetDetailResponse> findDetailById(Integer id) {

        String sql = """
                SELECT
                    a.*,
                    t.type_name,
                    d.department_name,
                    po.purchase_order_id,
                    po.created_at,
                    s.supplier_name
                FROM asset a

                LEFT JOIN asset_type t
                    ON a.asset_type_id = t.asset_type_id

                LEFT JOIN departments d
                    ON a.department_id = d.department_id

                LEFT JOIN purchase_order_details pod
                    ON a.purchase_order_detail_id = pod.purchase_order_detail_id

                LEFT JOIN purchase_orders po
                    ON pod.purchase_order_id = po.purchase_order_id

                LEFT JOIN supplier s
                    ON po.supplier_id = s.supplier_id

                WHERE a.asset_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AssetDetailResponse dto = new AssetDetailResponse();
                    dto.setAssetId(rs.getInt("asset_id"));
                    dto.setAssetName(rs.getString("asset_name"));

                    dto.setPurchaseOrderDetailId(rs.getInt("purchase_order_detail_id"));
                    dto.setOriginalCost(rs.getBigDecimal("original_cost"));

                    dto.setAssetTypeName(rs.getString("type_name"));

                    dto.setDepartmentName(rs.getString("department_name"));

                    dto.setAcquisitionDate(toLocalDate(rs.getDate("acquisition_date")));
                    dto.setInServiceDate(toLocalDate(rs.getDate("in_service_date")));
                    dto.setWarrantyStartDate(toLocalDate(rs.getDate("warranty_start_date")));
                    dto.setWarrantyEndDate(toLocalDate(rs.getDate("warranty_end_date")));

                    dto.setPurchaseOrderId(rs.getInt("purchase_order_id"));
                    dto.setOrderDate(toLocalDate(rs.getDate("created_at")));
                    dto.setSupplierName(rs.getString("supplier_name"));
                    return Optional.of(dto);

                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi lấy chi tiết tài sản", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Asset> searchAssets(String keyword, AssetStatus status, LocalDate fromDate,
            LocalDate toDate, String direction, int offset, int pageSize) {

        StringBuilder sql = new StringBuilder("""
                SELECT a.*, t.type_name
                FROM asset a
                LEFT JOIN asset_type t
                    ON a.asset_type_id = t.asset_type_id
                WHERE 1=1
                """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and a.asset_name like ? ");
        }
        if (status != null) {
            sql.append(" and a.current_status = ? ");
        }

        if (fromDate != null) {
            sql.append(" AND a.acquisition_date >= ? ");
        }

        if (toDate != null) {
            sql.append(" AND a.acquisition_date <= ? ");
        }

        // sort
        if (direction != null && !direction.isBlank()) {

            sql.append(" order by a.original_cost ");

            if ("DESC".equalsIgnoreCase(direction)) {
                sql.append(" DESC ");
            } else {
                sql.append(" ASC ");
            }

        } else {
            sql.append(" order by a.asset_id ");
        }

        sql.append(" offset ? rows fetch next ? rows only ");
        List<Asset> assets = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            if (status != null) {
                ps.setString(index++, status.name());
            }

            if (fromDate != null) {
                ps.setDate(index++, Date.valueOf(fromDate));
            }

            if (toDate != null) {
                ps.setDate(index++, Date.valueOf(toDate));
            }

            ps.setInt(index++, offset);
            ps.setInt(index, pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Asset asset = mapResultSet(rs);
                assets.add(asset);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return assets;
    }

    @Override
    public int countAssets(String keyword, AssetStatus status, LocalDate fromDate, LocalDate toDate) {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM asset a
                WHERE 1=1
                """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND a.asset_name LIKE ? ");
        }

        if (status != null) {
            sql.append(" AND a.current_status = ? ");
        }

        if (fromDate != null) {
            sql.append(" AND a.acquisition_date >= ?");
        }

        if (toDate != null) {
            sql.append(" AND a.acquisition_date <= ?");
        }

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            if (status != null) {
                ps.setString(index++, status.name());
            }

            if (fromDate != null) {
                ps.setDate(index++, Date.valueOf(fromDate));
            }

            if (toDate != null) {
                ps.setDate(index++, Date.valueOf(toDate));
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    @Override
    public List<Asset> findExpiringWarranties(int days) {
String sql ="select a.*, t.type_name from asset a\n" +
        "left join asset_type t on a.asset_type_id=t.asset_type_id\n" +
        "where a.warranty_end_date between  cast(getdate() as DATE) and dateadd(day, ?, cast(getdate() as date))\n" +
        "order by a.warranty_end_date asc";


        List<Asset> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }


    private Asset mapResultSet(ResultSet rs) throws SQLException {

        Asset asset = new Asset();

        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setPurchaseOrderDetailId(rs.getInt("purchase_order_detail_id"));
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