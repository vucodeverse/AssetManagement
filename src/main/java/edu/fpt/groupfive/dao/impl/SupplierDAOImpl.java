package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.dto.request.SupplierSearchCriteria;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierDAOImpl implements SupplierDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void createSupplier(Supplier supplier) {
        String query =
                "INSERT INTO supplier (" +
                        "supplier_name, " +
                        "phone_number, " +
                        "email, " +
                        "address, " +
                        "supplier_code, " +
                        "tax_code, " +
                        "status, " +
                        "created_date, " +
                        "updated_date" +
                        ") VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', GETDATE(), GETDATE())";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getPhoneNumber());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getAddress());
            ps.setString(5, supplier.getSupplierCode());
            ps.setString(6, supplier.getTaxCode());
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Thêm nhà cung cấp thất bại: " + supplier.getSupplierCode());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo nhà cung cấp với mã: " + supplier.getSupplierCode(), e);
        }
    }

    @Override
    public int updateBySupplierCode(Supplier supplier) {
        String query =
                "UPDATE supplier SET " +
                        "supplier_name = ?, " +
                        "tax_code = ?, " +
                        "phone_number = ?, " +
                        "email = ?, " +
                        "address = ?, " +
                        "updated_date = GETDATE() " +
                        "WHERE supplier_code = ? " +
                        "AND status = 'ACTIVE'";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getTaxCode());
            ps.setString(3, supplier.getPhoneNumber());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setString(6, supplier.getSupplierCode());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật nhà cung cấp với mã: " + supplier.getSupplierCode(), e);
        }
    }

    @Override
    public int deactivateBySupplierCode(String supplierCode) {
        String query =
                "UPDATE supplier SET " +
                        "status = 'INACTIVE', " +
                        "updated_date = GETDATE() " +
                        "WHERE supplier_code = ? " +
                        "AND status = 'ACTIVE' " +
                        "AND NOT EXISTS ( " +
                        "   SELECT 1 FROM purchase_order po " +
                        "   JOIN supplier s ON s.supplier_id = po.supplier_id " +
                        "   WHERE s.supplier_code = ? " +
                        "   AND po.status NOT IN ('CANCELLED', 'COMPLETED')" +
                        ")";

        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplierCode);
            ps.setString(2, supplierCode);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể vô hiệu hóa nhà cung cấp với mã: " + supplierCode, e);
        }
    }

    @Override
    public Optional<Supplier> findBySupplierCode(String supplierCode) {
        String query = """
            SELECT supplier_id, supplier_name, phone_number, email, address,
                   supplier_code, tax_code, status, created_date, updated_date
            FROM supplier
            WHERE supplier_code = ?
        """;
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplierCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể tìm nhà cung cấp với mã: " + supplierCode, e);
        }
    }

    //paginated search
    @Override
    public List<Supplier> search(
            SupplierSearchCriteria criteria,
            int offset,
            int size,
            String sortField,
            String sortDir) {

        StringBuilder query = new StringBuilder(
                "SELECT supplier_id, supplier_name, phone_number, email, address, " +
                        "supplier_code, tax_code, status, created_date, updated_date " +
                        "FROM supplier WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        appendFilters(query, params, criteria);

        String orderByColumn;
        if (sortField == null || sortField.isBlank()) {
            sortField = "supplierCode";
        }
        switch (sortField) {
            case "supplierName" -> orderByColumn = "supplier_name";
            case "taxCode" -> orderByColumn = "tax_code";
            case "createdDate" -> orderByColumn = "created_date";
            case "status" -> orderByColumn = "status";
            default -> orderByColumn = "supplier_code";
        }

        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        query.append(" ORDER BY ")
                .append(orderByColumn)
                .append(" ")
                .append(direction)
                .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        params.add(offset);
        params.add(size);

        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query.toString())
        ) {
            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<Supplier> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm kiếm danh sách nhà cung cấp.", e);
        }
    }

    @Override
    public int countSearch(SupplierSearchCriteria criteria) {
        StringBuilder query = new StringBuilder(
                "SELECT COUNT(*) FROM supplier WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        appendFilters(query, params, criteria);
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query.toString())
        ) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đếm số lượng nhà cung cấp theo điều kiện tìm kiếm", e);
        }
    }
    private void appendFilters(StringBuilder query,
                               List<Object> params,
                               SupplierSearchCriteria criteria) {

        if (criteria.getSupplierCode() != null &&
                !criteria.getSupplierCode().isBlank()) {

            query.append(" AND supplier_code LIKE ?");
            params.add("%" + criteria.getSupplierCode().trim() + "%");
        }

        if (criteria.getSupplierName() != null &&
                !criteria.getSupplierName().isBlank()) {

            query.append(" AND supplier_name LIKE ?");
            params.add("%" + criteria.getSupplierName().trim() + "%");
        }

        if (criteria.getStatus() != null &&
                !criteria.getStatus().isBlank()) {

            query.append(" AND status = ?");
            params.add(criteria.getStatus());
        }

        if (criteria.getCreatedFrom() != null) {
            query.append(" AND created_date >= ?");
            params.add(Timestamp.valueOf(criteria.getCreatedFrom()));
        }

        if (criteria.getCreatedTo() != null) {
            query.append(" AND created_date <= ?");
            params.add(Timestamp.valueOf(criteria.getCreatedTo()));
        }
    }
    private void setParameters(PreparedStatement ps,
                               List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof String) ps.setString(i + 1, (String) value);
            else if (value instanceof Integer) ps.setInt(i + 1, (Integer) value);
            else if (value instanceof Timestamp) ps.setTimestamp(i + 1, (Timestamp) value);
            else ps.setObject(i + 1, value);
        }
    }
    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setSupplierName(rs.getString("supplier_name"));
        supplier.setPhoneNumber(rs.getString("phone_number"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        supplier.setSupplierCode(rs.getString("supplier_code"));
        supplier.setTaxCode(rs.getString("tax_code"));
        supplier.setStatus(rs.getString("status"));
        supplier.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_date");
        if (updated != null) {
            supplier.setUpdatedDate(updated.toLocalDateTime());
        }
        return supplier;
    }



}
