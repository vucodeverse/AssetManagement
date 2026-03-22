package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuotationDAOImpl implements QuotationDAO {
    private final DatabaseConfig databaseConfig;
    private final QuotationDetailDAO quotationDetailDAO;

    @Value("${dao.common.insert_error}")
    private String insertErrorMsg;

    @Value("${purchase.create.failure}")
    private String createFailureMsg;

    @Value("${dao.common.find_error}")
    private String findErrorMsg;

    private Quotation mapResultSetToQuotation(ResultSet rs) throws SQLException {
        Quotation quotation = new Quotation();

        quotation.setId(rs.getInt("quotation_id"));
        quotation.setPurchaseId(rs.getInt("purchase_request_id"));
        quotation.setSupplierId(rs.getInt("supplier_id"));
        quotation.setQuotationStatus(
                PurchaseProcessStatus.valueOf(rs.getString("status").toUpperCase()));
        quotation.setTotalAmount(rs.getBigDecimal("total_amount"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        quotation.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        quotation.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

        return quotation;
    }

    @Override
    public Integer insert(Quotation quotation) {

        String sql = "insert into quotation (purchase_request_id, supplier_id, status, total_amount, " +
                "created_at) values (?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setObject(1, quotation.getPurchaseId());
                preparedStatement.setObject(2, quotation.getSupplierId());
                preparedStatement.setString(3, quotation.getQuotationStatus().name());
                preparedStatement.setBigDecimal(4, quotation.getTotalAmount());
                preparedStatement.setTimestamp(5,
                        quotation.getCreatedAt() != null ? Timestamp.valueOf(quotation.getCreatedAt())
                                : Timestamp.valueOf(LocalDateTime.now()));

                preparedStatement.executeUpdate();
                ResultSet rs = preparedStatement.getGeneratedKeys();

                int quotationId = 0;
                if (rs.next()) {
                    quotationId = rs.getInt(1);
                }

                // insert details cùng 1 connection
                if (quotationId != 0 && quotation.getQuotationDetails() != null) {
                    for (QuotationDetail qd : quotation.getQuotationDetails()) {
                        qd.setQuotationId(quotationId);
                        quotationDetailDAO.insert(qd, connection);
                    }
                }

                connection.commit();
                return quotationId;
            } catch (SQLException e) {
                connection.rollback();

                throw new DataAccessException(insertErrorMsg, e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException(createFailureMsg, e);
        }

    }

    // update khi save draft
    @Override
    public void update(Quotation quotation) {
        String sql = "update quotation set supplier_id = ?, status = ?, total_amount = ?, updated_at = ? " +
                "where quotation_id = ? and status in ('DRAFT', 'PENDING')";

        Connection connection = null;
        try {

            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, quotation.getSupplierId());
            preparedStatement.setString(2, quotation.getQuotationStatus().name());
            preparedStatement.setBigDecimal(3, quotation.getTotalAmount());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setInt(5, quotation.getId());
            preparedStatement.executeUpdate();

            // xóa detail cũ rồi insert lại
            quotationDetailDAO.deleteByQuotationId(quotation.getId(), connection);

            if (quotation.getQuotationDetails() != null) {
                for (QuotationDetail qd : quotation.getQuotationDetails()) {
                    qd.setQuotationId(quotation.getId());
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
    public void updateStatus(Integer quotationId, PurchaseProcessStatus status) {
        String sql = "update quotation set status = ?, updated_at = ? WHERE quotation_id =" +
                " ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, status.name());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setInt(3, quotationId);
            preparedStatement.executeUpdate();

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
                } catch (SQLException ignored) {
                }
        }
    }

    // tìm kiếm theo quotation id
    @Override
    public Optional<Quotation> findById(Integer quotationId) {

        String sql = "select * from quotation where quotation_id = ? and status <> 'DELETED'";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, quotationId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToQuotation(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }

        return Optional.empty();
    }

    // tìm kiếm theo purchase id
    @Override
    public List<Quotation> findByPurchaseId(Integer purchaseId) {

        String sql = "select * from quotation where purchase_request_id = ? and status <> 'DELETED'";

        List<Quotation> quotations = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                quotations.add(mapResultSetToQuotation(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return quotations;
    }

    // tinsh số lượng quotation theo từng purchase
    @Override
    public Integer countQuotationFromPurchaseId(Integer purchaseId) {

        String sql = "select count(distinct q.quotation_id) from purchase_request p " +
                "left join quotation q on p.purchase_request_id = q.purchase_request_id " +
                "where p.purchase_request_id = ? and (q.status <> 'DELETED') " +
                "group by p.purchase_request_id";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return 0;
    }

    // saerch và filter cho màn quotation of purchase
    @Override
    public List<Quotation> searchByPurchaseId(QuotationSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select q.* from quotation q " +
                        "join supplier s on q.supplier_id = s.supplier_id " +
                        "where q.status <> 'DELETED' ");

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
                quotation.setQuotationStatus(PurchaseProcessStatus.valueOf(rs.getString("status").toUpperCase()));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                quotation.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                quotation.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

                quotations.add(quotation);
            }

        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return quotations;
    }

    // lấy ra những quotaiton đc thêm
    @Override
    public List<Quotation> findAll() {
        String sql = "select q.*, s.supplier_name from quotation q " +
                "join supplier s on q.supplier_id = s.supplier_id " +
                "where q.status <> 'DELETED'";
        List<Quotation> quotations = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                quotations.add(mapResultSetToQuotation(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return quotations;
    }

}