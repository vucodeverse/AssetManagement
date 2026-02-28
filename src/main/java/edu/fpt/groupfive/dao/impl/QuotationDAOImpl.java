package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.model.QuotationDetail;
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
public class QuotationDAOImpl implements QuotationDAO {
    private final DatabaseConfig databaseConfig;
    private final QuotationDetailDAO quotationDetailDAO;

    @Override
    public Integer insert(Quotation quotation) {

        String sql = "insert into quotation (purchase_request_id, supplier_id, status, total_amount, " +
                "created_at, rejected_reason) VALUES (?,?,?,?,?,?)";
        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setObject(1, quotation.getPurchaseId());
            preparedStatement.setObject(2, quotation.getSupplierId());
            preparedStatement.setString(3, quotation.getQuotationStatus().toString());
            preparedStatement.setBigDecimal(4, quotation.getTotalAmount());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(quotation.getCreatedAt().atStartOfDay()));
            preparedStatement.setString(6, quotation.getRejectedReason());

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            int quotationId = 0;
            if (rs.next()) {
                quotationId = rs.getInt(1);
            }

            // insert details cùng 1 connection/transaction
            if (quotationId != 0 && quotation.getQuotationDetails() != null) {
                for (QuotationDetail qd : quotation.getQuotationDetails()) {
                    qd.setQuotationId(quotationId);
                    quotationDetailDAO.insert(qd, connection);
                }
            }

            connection.commit();
            return quotationId;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {

                }
            }

            throw new RuntimeException(e);
        } finally {

            // reset auto commit lại về true và đóng cổng.
            if (connection != null)
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception ignored) {
                }
        }
    }

    @Override
    public void update(Quotation quotation) {
        String sql = "update quotation set supplier_id = ?, status = ?, total_amount = ?, updated_at = GETDATE() " +
                "where quotation_id = ?";

        Connection connection = null;
        try {

            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, quotation.getSupplierId());
            preparedStatement.setString(2, quotation.getQuotationStatus().toString());
            preparedStatement.setBigDecimal(3, quotation.getTotalAmount());
            preparedStatement.setInt(4, quotation.getId());
            preparedStatement.executeUpdate();

            // xóa detail cũ rồi insert lại (cùng 1 connection/transaction)
            quotationDetailDAO.deleteByQuotationId(quotation.getId(), connection);

            if (quotation.getQuotationDetails() != null) {
                for (QuotationDetail qd : quotation.getQuotationDetails()) {
                    quotationDetailDAO.insert(qd, connection);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (connection != null)
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception ignored) {
                }
        }
    }

    @Override
    public void updateStatusReject(Integer quotationId, QuotationStatus status, String rejectedReason) {
        String sql = "UPDATE quotation SET status = ?, rejected_reason = ?, updated_at = GETDATE() WHERE quotation_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, status.toString());
            preparedStatement.setString(2, rejectedReason);
            preparedStatement.setInt(3, quotationId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (connection != null)
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {
                }
        }
    }

    // tìm kiếm theo quotation id
    @Override
    public Optional<Quotation> findById(Integer quotationId) {

        String sql = "select * from quotation where quotation_id = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, quotationId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {

                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setQuotationStatus(QuotationStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation
                        .setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                quotation
                        .setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                quotation.setRejectedReason(rs.getString("reject_reason"));

                return Optional.of(quotation);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    // tìm kiếm theo purchase id
    @Override
    public List<Quotation> findByPurchaseId(Integer purchaseId) {

        String sql = "select * from quotation where purchase_request_id = ?";

        List<Quotation> quotations = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {

                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setQuotationStatus(QuotationStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation
                        .setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                quotation
                        .setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                quotations.add(quotation);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotations;
    }

    @Override
    public Optional<Quotation> findResponseById(Integer quotationId) {
        String sql = "SELECT q.*, s.supplier_name " +
                "FROM quotation q " +
                "JOIN supplier s ON q.supplier_id = s.supplier_id " +
                "WHERE q.quotation_id = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, quotationId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setQuotationStatus(QuotationStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation
                        .setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                quotation
                        .setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);

                return Optional.of(quotation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Quotation> findResponsesByPurchaseId(Integer purchaseId) {
        return List.of();
    }

    @Override
    public List<Quotation> getAll() {
        return List.of();
    }

    // tinsh số lượng quotation theo từng purchase
    @Override
    public Integer countQuotationFromPurchaseId(Integer purchaseId) {

        String sql = "select p.purchase_request_id, count(distinct q.quotation_id) from purchase_request p left join quotation q on p\n"
                +
                "    .purchase_request_id = q\n" +
                "    .purchase_request_id where p\n" +
                "    .purchase_request_id = ? group by p.purchase_request_id";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(2);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    // saerch và filter cho màn quotation of purchase
    @Override
    public List<Quotation> searchAndFilterQuotationOfPurchase(QuotationSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select q.* from quotation q " +
                        "join supplier s on q.supplier_id = s.supplier_id " +
                        "where 1=1 ");

        List<Object> params = new ArrayList<>();

        if (criteria.getPurchaseId() != null) {
            sql.append(" and q.purchase_request_id = ? ");
            params.add(criteria.getPurchaseId());
        }

        if (criteria.getSupplierName() != null && !criteria.getSupplierName().isBlank()) {
            sql.append(" and s.supplier_name = ? ");
            params.add(criteria.getSupplierName());
        }

        if (criteria.getStatus() != null) {
            sql.append(" and q.status = ? ");
            params.add(criteria.getStatus().name());
        }

        if (criteria.getMinAmount() != null) {
            sql.append(" and q.total_amount >= ? ");
            params.add(criteria.getMinAmount());
        }

        if (criteria.getMaxAmount() != null) {
            sql.append(" and q.total_amount <= ? ");
            params.add(criteria.getMaxAmount());
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            sql.append(" and ( ");
            if (criteria.getKeyword().matches("\\d+")) {
                sql.append(" q.quotation_id = ? or ");
                params.add(Integer.parseInt(criteria.getKeyword()));
            }
            sql.append(" s.supplier_name like ? ) ");
            params.add("%" + criteria.getKeyword() + "%");
        }

        List<Quotation> quotations = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setQuotationStatus(QuotationStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation
                        .setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                quotation
                        .setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                quotations.add(quotation);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotations;
    }

    // đếm số lượng quotaiton dựa trên status
    @Override
    public long countByStatus(QuotationStatus status) {
        String sql = "select count(*) from quotation where status = ?";
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, status.name());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    // lấy ra những quotaiton đc thêm gần đây
    @Override
    public List<Quotation> findRecent(int limit) {
        String sql = "select q.*, s.supplier_name from quotation q " +
                "join supplier s on q.supplier_id = s.supplier_id " +
                "order by q.created_at desc, q.quotation_id desc " +
                "offset 0 rows fetch next ? rows only";
        List<Quotation> quotations = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, limit);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setQuotationStatus(QuotationStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation
                        .setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                quotation
                        .setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                quotations.add(quotation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotations;
    }

    @Override
    public BigDecimal totalAmountForPurchaseId(Integer purchaseId) {

        String sql = "select min(q.total_amount) from quotation q where q.purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {

                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return BigDecimal.ZERO;
    }
}