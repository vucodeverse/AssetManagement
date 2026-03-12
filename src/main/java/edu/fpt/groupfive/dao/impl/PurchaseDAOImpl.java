package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PurchaseDAOImpl implements PurchaseDAO {

    private final DatabaseConfig databaseConfig;
    private final PurchaseDetailDAO purchaseDetailDAO;

    // map các thuộc tính
    private Purchase mapRowForList(ResultSet rs) throws SQLException {
        Purchase purchase = new Purchase();
        purchase.setId(rs.getInt("purchase_request_id"));

        purchase.setStatus(Request.valueOf(rs.getString("status").trim().toUpperCase()));

        purchase.setPurchaseNote(rs.getString("note"));
        purchase.setRejectReason(rs.getString("reject_reason"));
        purchase.setCreatedByUser(rs.getInt("creator_id"));

        Date neededByDate = rs.getDate("needed_by_date");
        purchase.setNeededByDate(neededByDate != null ? neededByDate.toLocalDate() : null);
        purchase.setReason(rs.getString("request_reason"));

        purchase.setPriority(Priority.valueOf(rs.getString("priority").trim().toUpperCase()));

        // sửa thành LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        purchase.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));
        Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
        if (approvedAt != null) {
            purchase.setApprovedAt(approvedAt.toLocalDateTime());
        }
        purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        purchase.setUpdatedAt(updatedAtTs != null ? updatedAtTs.toLocalDateTime() : null);
        return purchase;
    }

    // insert purchase request
    @Override
    public int insert(Purchase purchase) {

        String sql = "insert into purchase_request (status, note, creator_id, needed_by_date, priority, " +
                "approved_by_director_id,reject_reason, created_at, updated_at, request_reason, " +
                "approved_by_director_at," +
                "purchase_staff_user_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {

                preparedStatement.setString(1, purchase.getStatus().name());
                preparedStatement.setString(2, purchase.getPurchaseNote());
                preparedStatement.setInt(3, purchase.getCreatedByUser());
                preparedStatement.setDate(4, Date.valueOf(purchase.getNeededByDate()));
                preparedStatement.setString(5, purchase.getPriority().name());
                preparedStatement.setObject(6, purchase.getApprovedByDirector());
                preparedStatement.setString(7, purchase.getRejectReason());
                preparedStatement.setTimestamp(8, Timestamp.valueOf(purchase.getCreatedAt()));
                preparedStatement.setTimestamp(9,
                        purchase.getUpdatedAt() != null
                                ? Timestamp.valueOf(purchase.getUpdatedAt())
                                : null);
                preparedStatement.setString(10, purchase.getReason());
                preparedStatement.setTimestamp(11,
                        purchase.getApprovedAt() != null
                                ? Timestamp.valueOf(purchase.getApprovedAt())
                                : null);
                preparedStatement.setObject(12, purchase.getPurchaseStaffId());

                preparedStatement.executeUpdate();

                int purchaseId;
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    purchaseId = rs.next() ? rs.getInt(1) : 0;
                }

                if (purchase.getPurchaseDetails() != null) {
                    for (PurchaseDetail d : purchase.getPurchaseDetails()) {
                        d.setPurchaseRequestId(purchaseId);
                        purchaseDetailDAO.insert(d, connection);
                    }
                }

                connection.commit();
                return purchaseId;

            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new DataAccessException("Thêm yêu cầu mua sắm thất bại", e);
        }
    }

    // tìm kiếm purchase theo từng id
    @Override
    public Optional<Purchase> findById(Integer purchaseId) {

        String sql = "select p.*, u.first_name, u.last_name " +
                "from purchase_request p left join users u on p.creator_id = u.user_id " +
                "where p.purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Purchase purchase = mapRowForList(rs);

                // load riêng từng purchase detail
                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId));
                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }

        return Optional.empty();
    }

    // lấy ra các purchase đã dc approve
    @Override
    public Optional<Purchase> findByIdAndStatus(Integer purchaseId, String status) {
        String sql = "select * from purchase_request where purchase_request_id = ? and status = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, purchaseId);
            preparedStatement.setString(2, status);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {

                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("purchase_request_id"));
                purchase.setStatus(Request.valueOf(rs.getString("status").toUpperCase()));
                purchase.setPurchaseNote(rs.getString("note"));
                purchase.setRejectReason(rs.getString("reject_reason"));
                purchase.setCreatedByUser(rs.getInt("creator_id"));
                Date neededByDate = rs.getDate("needed_by_date");
                purchase.setNeededByDate(
                        neededByDate != null ? neededByDate.toLocalDate() : null);

                purchase.setReason(rs.getString("request_reason"));
                purchase.setPriority(Priority.valueOf(rs.getString("priority").toUpperCase()));
                purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));

                Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
                if (approvedAt != null) {
                    purchase.setApprovedAt(approvedAt.toLocalDateTime());
                }

                purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
                purchase.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
                purchase.setUpdatedAt(
                        updatedAt != null ? updatedAt : null);

                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId));

                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }

        return Optional.empty();
    }

    // lấy tất cả các purchase
    @Override
    public List<Purchase> findAll() {
        String sql = "select p.* " +
                "from purchase_request p where p.status <> 'DELETED'";

        List<Purchase> purchases = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                try {
                    purchases.add(mapRowForList(rs));
                } catch (Exception e) {
                    throw new DataAccessException("Lỗi không thể map dữ liệu", e);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }
        return purchases;
    }

    // lấy purchase lọc theo filter và search
    @Override
    public List<Purchase> getPurchaseByFilter(PurchaseRequestSearchCriteria p) {

        // khai báo dynamic sql
        StringBuilder sql = new StringBuilder(
                "select p.*" +
                        "from purchase_request p left join users u on p.creator_id = u.user_id where 1 = 1 and p" +
                        ".status <> 'DELETED'");
        List<Purchase> purchases = new ArrayList<>();

        List<Object> params = new ArrayList<>();

        if (p.getStatus() != null) {
            sql.append(" and p.status = ? ");
            params.add(p.getStatus().name());
        }

        if (p.getPriority() != null) {
            sql.append(" and p.priority = ? ");
            params.add(p.getPriority().name());
        }

        if (p.getFrom() != null) {
            sql.append(" and p.needed_by_date >= ? ");
            params.add(Date.valueOf(p.getFrom()));
        }

        if (p.getTo() != null) {
            sql.append(" and p.needed_by_date < ? ");
            params.add(Date.valueOf(p.getTo()));
        }

        if (p.getKeyword() != null && !p.getKeyword().isBlank()) {
            sql.append(" and (");
            String keyword = p.getKeyword().trim();

            if (keyword.matches("\\d+")) {
                sql.append(" p.purchase_request_id = ? or ");
                params.add(Integer.parseInt(keyword));
            }

            sql.append(" lower(u.first_name) like ? or lower(u.last_name) like ? )");

            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i++, param);
            }
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                try {
                    Purchase mappedPurchase = mapRowForList(rs);
                    purchases.add(mappedPurchase);
                } catch (Exception e) {
                    throw new DataAccessException("Lỗi không thể map dữ liệu", e);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }
        return purchases;
    }

    // update purchase
    @Override
    public void updatePurchaseStatus(Request request, Integer purchaseId, String reasonReject, Integer userId) {

        String sql = "update purchase_request set status = ? , reject_reason = ?, updated_at = ?, " +
                "approved_by_director_at = ?, approved_by_director_id = ? where purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, request.name());

            if (reasonReject == null || reasonReject.isBlank()) {
                preparedStatement.setNull(2, Types.NVARCHAR);
            } else {
                preparedStatement.setString(2, reasonReject);
            }

            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            if ("APPROVED".equals(request.name())) {
                preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                preparedStatement.setObject(5, userId);
            } else {
                preparedStatement.setNull(4, Types.TIMESTAMP);
                preparedStatement.setNull(5, Types.INTEGER);
            }

            preparedStatement.setInt(6, purchaseId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }
    }

    // lấy ra từng số lượng quotation và min totalamount của từng quotation theo
    // từng purchase kèm các thuộc tính cần
    // của purchase
    @Override
    public Map<Integer, Object[]> findQuotaSummaryByFilter(QuotationSearchCriteria s) {
        StringBuilder sql = new StringBuilder(
                "select p.purchase_request_id, p.needed_by_date, p.priority, " +
                        "count(q.quotation_id) as number_of_quotation, " +
                        "min(q.total_amount) as est_price " +
                        "from purchase_request p " +
                        "left join quotation q on p.purchase_request_id = q.purchase_request_id " +
                        "left join users u on p.creator_id = u.user_id " +
                        "where p.status = 'APPROVED' ");

        List<Object> params = new ArrayList<>();

        if (s.getPriority() != null) {
            sql.append(" and p.priority = ? ");
            params.add(s.getPriority().name());
        }

        if (s.getFrom() != null) {
            sql.append(" and p.needed_by_date >= ? ");
            params.add(Date.valueOf(s.getFrom()));
        }

        if (s.getTo() != null) {
            sql.append(" and p.needed_by_date <= ? ");
            params.add(Date.valueOf(s.getTo()));
        }

        if (s.getKeyword() != null && !s.getKeyword().isBlank()) {
            sql.append(" and ( ");
            if (s.getKeyword().matches("\\d+")) {
                sql.append(" p.purchase_request_id = ? or ");
                params.add(Integer.parseInt(s.getKeyword()));
            }
            sql.append(" u.first_name like ? or u.last_name like ? ) ");
            String keyword = "%" + s.getKeyword().toLowerCase() + "%";
            params.add(keyword);
            params.add(keyword);
        }

        sql.append(" group by p.purchase_request_id, p.needed_by_date, p.priority ");

        // nếu có ddkien search theo total
        if (s.getMinAmount() != null || s.getMaxAmount() != null) {
            sql.append(" having 1=1 ");
            if (s.getMinAmount() != null) {
                sql.append(" and min(q.total_amount) >= ? ");
                params.add(s.getMinAmount());
            }
            if (s.getMaxAmount() != null) {
                sql.append(" and min(q.total_amount) <= ? ");
                params.add(s.getMaxAmount());
            }
        }

        Map<Integer, Object[]> result = new LinkedHashMap<>();

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int purchaseId = rs.getInt("purchase_request_id");
                    Date neededByDate = rs.getDate("needed_by_date");
                    result.put(purchaseId, new Object[] {
                            neededByDate.toLocalDate(),
                            rs.getString("priority"),
                            rs.getInt("number_of_quotation"),
                            rs.getBigDecimal("est_price")
                    });
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }

        return result;
    }

    // update purchase request nếu là draft
    @Override
    public void update(Purchase purchase) {

        String sql = "update purchase_request set status = ?, note = ?, needed_by_date = ?, " +
                "priority = ?, reject_reason = ?, updated_at = ?, request_reason = ? " +
                "where purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setString(1, purchase.getStatus().name());
                ps.setString(2, purchase.getPurchaseNote());
                ps.setDate(3, Date.valueOf(purchase.getNeededByDate()));
                ps.setString(4, purchase.getPriority().name());
                ps.setString(5, purchase.getRejectReason());
                ps.setTimestamp(6,
                        purchase.getUpdatedAt() != null
                                ? Timestamp.valueOf(purchase.getUpdatedAt())
                                : null);
                ps.setString(7, purchase.getReason());
                ps.setInt(8, purchase.getId());

                ps.executeUpdate();
            }

            purchaseDetailDAO.deleteByPurchaseRequestId(purchase.getId(), connection);

            if (purchase.getPurchaseDetails() != null) {
                for (PurchaseDetail d : purchase.getPurchaseDetails()) {
                    d.setPurchaseRequestId(purchase.getId());
                    purchaseDetailDAO.insert(d, connection);
                }
            }

            connection.commit();

        } catch (Exception e) {
            throw new DataAccessException("Lỗi khi chèn dữ liệu", e);
        }
    }
}
