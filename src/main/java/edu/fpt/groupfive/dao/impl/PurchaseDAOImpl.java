package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PurchaseDAOImpl implements PurchaseDAO {

    private final DatabaseConfig databaseConfig;
    private final PurchaseDetailDAO purchaseDetailDAO;

    // insert purchase request
    @Override
    public int insert(Purchase purchase) {

        // câu sql
        String sql = "insert into purchase_request (status, note, creator_id, needed_by_date, priority, " +
                "approved_by_director_id,reject_reason, created_at, updated_at, request_reason, " +
                "approved_by_director_at," +
                "purchase_staff_user_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();

            // tắt auto commit
            connection.setAutoCommit(false);

            // kết nối db và trả lại id sau khi insert thành công
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // set placeholder
            preparedStatement.setString(1, purchase.getStatus() != null ? purchase.getStatus().toString() : null);
            preparedStatement.setString(2, purchase.getNote());
            preparedStatement.setInt(3, purchase.getCreatedByUser());

            if (purchase.getNeededByDate() != null) {
                preparedStatement.setDate(4, Date.valueOf(purchase.getNeededByDate()));
            } else {
                preparedStatement.setNull(4, Types.DATE);
            }

            if (purchase.getPriority() != null) {
                preparedStatement.setString(5, purchase.getPriority().name());
            } else {
                preparedStatement.setNull(5, Types.NVARCHAR);
            }

            preparedStatement.setObject(6, purchase.getApprovedByDirector());
            preparedStatement.setString(7, purchase.getRejectReason());

            if (purchase.getCreatedAt() != null) {
                preparedStatement.setTimestamp(8, Timestamp.valueOf(purchase.getCreatedAt().atStartOfDay()));
            } else {
                preparedStatement.setNull(8, Types.TIMESTAMP);
            }

            preparedStatement.setTimestamp(9,
                    purchase.getUpdatedAt() != null ? Timestamp.valueOf(purchase.getUpdatedAt().atStartOfDay()) : null);

            preparedStatement.setString(10, purchase.getReason());
            preparedStatement.setTimestamp(11,
                    purchase.getApprovedAt() != null ? Timestamp.valueOf(purchase.getApprovedAt()) : null);
            preparedStatement.setObject(12, purchase.getPurchaseStaffId());

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            int purchaseId = rs.next() ? rs.getInt(1) : 0;

            if (purchase.getPurchaseDetails() != null) {
                for (PurchaseDetail d : purchase.getPurchaseDetails()) {
                    d.setPurchaseRequestId(purchaseId);

                    // truyền cả connection nếu lỗi sẽ rollback hết
                    purchaseDetailDAO.insert(d, connection);
                }
            }

            connection.commit();
            return purchaseId;

        } catch (Exception exception) {

            // nếu có lỗi sẽ rollback
            if (connection != null) try {
                connection.rollback();
            } catch (Exception ignored) {}
            throw new RuntimeException(exception);
        } finally {

            // reset auto commit lại về true và đóng cổng.
            if (connection != null) try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception ignored) {}
        }
    }

    // tìm kiếm purchase theo từng id
    @Override
    public Optional<Purchase> findById(Integer purchaseId) {

        String sql = "select p.*, u.first_name, u.last_name " +
                "from purchase_request p left join users u on p.creator_id = u.user_id " +
                "where p.purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                Purchase purchase = mapRowForList(rs);
                // load riêng từng purchase detaily
                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId)
                );
                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    // lấy ra các purchase đã dc approve
    @Override
    public Optional<Purchase> findByIdAndApproved(Integer purchaseId, String status) {
        String sql = "select * from purchase_request where purchase_request_id = ? and status = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            preparedStatement.setString(2, status);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){

                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("purchase_request_id"));
                purchase.setStatus(Request.valueOf(rs.getString("status").toUpperCase()));
                purchase.setNote(rs.getString("note"));
                purchase.setRejectReason(rs.getString("reject_reason"));
                purchase.setCreatedByUser(rs.getInt("creator_id"));
                Date neededByDate = rs.getDate("needed_by_date");
                purchase.setNeededByDate(
                        neededByDate != null ? neededByDate.toLocalDate() : null
                );

                purchase.setReason(rs.getString("request_reason"));
                purchase.setPriority(Priority.valueOf(rs.getString("priority").toUpperCase()));
                purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));

                Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
                if (approvedAt != null) {
                    purchase.setApprovedAt(approvedAt.toLocalDateTime());
                }

                purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
                purchase.setCreatedAt(rs.getDate("created_at").toLocalDate());
                Date updatedAt = rs.getDate("updated_at");
                purchase.setUpdatedAt(
                        updatedAt != null ? updatedAt.toLocalDate() : null
                );

                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId)
                );

                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    // lấy tất cả các purchase
    @Override
    public List<Purchase> findAll() {
        String sql = "select p.*, u.first_name, u.last_name " +
                "from purchase_request p left join users u on p.creator_id = u.user_id";

        List<Purchase> purchases = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                try {
                    purchases.add(mapRowForList(rs));
                } catch (Exception e) {
                    log.error("Error mapping purchase row: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return purchases;
    }

    // map các thuộc tính
    private Purchase mapRowForList(ResultSet rs) throws SQLException {
        Purchase purchase = new Purchase();
        purchase.setId(rs.getInt("purchase_request_id"));

        String status = rs.getString("status");
        if (status != null && !status.isBlank()) {
            try {
                purchase.setStatus(Request.valueOf(status.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
            }
        }

        purchase.setNote(rs.getString("note"));
        purchase.setRejectReason(rs.getString("reject_reason"));
        purchase.setCreatedByUser(rs.getInt("creator_id"));

        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        if (firstName != null || lastName != null) {
            purchase.setCreatorName(
                    ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim()
            );
        }

        Date neededByDate = rs.getDate("needed_by_date");
        purchase.setNeededByDate(neededByDate != null ? neededByDate.toLocalDate() : null);
        purchase.setReason(rs.getString("request_reason"));

        String priority = rs.getString("priority");
        if (priority != null && !priority.isBlank()) {
            try {
                purchase.setPriority(Priority.valueOf(priority.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid priority value: {}", priority);
            }
        }

        Date createdAt = rs.getDate("created_at");
        purchase.setCreatedAt(createdAt != null ? createdAt.toLocalDate() : null);

        purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));
        Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
        if (approvedAt != null) {
            purchase.setApprovedAt(approvedAt.toLocalDateTime());
        }
        purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
        Date updatedAt = rs.getDate("updated_at");
        purchase.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDate() : null);
        return purchase;
    }

    // lấy purchase theo các thuộc tính trong filter
    @Override
    public List<Purchase> getPurchaseByFilter(PurchaseSearchAndFilter p) {

        // khai báo dynamic sql
        StringBuilder sql = new StringBuilder(
                "select p.*, u.first_name, u.last_name " +
                "from purchase_request p left join users u on p.creator_id = u.user_id where 1 = 1");
        List<Purchase> purchases = new ArrayList<>();

        List<Object> params = new ArrayList<>();

        if(p.getStatus() != null){
            sql.append(" and p.status = ? ");
            params.add(p.getStatus().name());
        }

        if(p.getPriority() != null){
            sql.append(" and p.priority = ? ");
            params.add(p.getPriority().name());
        }

        if(p.getFrom() != null){
            sql.append(" and p.needed_by_date >= ? ");
            params.add(Date.valueOf(p.getFrom()));
        }

        if(p.getTo() != null){
            sql.append(" and p.needed_by_date < ? ");
            params.add(Date.valueOf(p.getTo()));
        }

        if(p.getKeyword() != null && !p.getKeyword().isBlank()){
            sql.append(" and (");
            String keyword = p.getKeyword().trim();

            // validte keyword
            String idStr = keyword;
            if (idStr.toUpperCase().startsWith("PR-")) {
                idStr = idStr.substring(3);
            }

            //nếu là số thì search theo id
            if (idStr.matches("\\d+")) {
                sql.append(" p.purchase_request_id = ? or ");
                params.add(Integer.parseInt(idStr));
            }

            sql.append(" lower(u.first_name) like ? or lower(u.last_name) like ? )");

            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())){

            int i = 1;
            for(Object param : params){
                preparedStatement.setObject(i++, param);
            }
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                try {
                    Purchase mappedPurchase = mapRowForList(rs);
                    purchases.add(mappedPurchase);
                } catch (Exception e) {
                    log.error("Error mapping filtered purchase row: {}", e.getMessage());
                }
            }

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return purchases;
    }

    // update status của purchase
    @Override
    public void updatePurchaseStatus(Request request, Integer purchaseId, String reasonReject) {
        String sql = "update purchase_request set status = ? , reject_reason = ? where purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setString(1,request.name());
            preparedStatement.setInt(3,purchaseId);

            if (reasonReject == null || reasonReject.isBlank()) {
                preparedStatement.setNull(2, Types.NVARCHAR);
            } else {
                preparedStatement.setString(2, reasonReject);
            }
            preparedStatement.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    // lấy ra từng số lượng quotation và min totalamount của từng quotation theo từng purchase kèm các thuộc tính cần
    // của purchase
    @Override
    public Map<Integer, Object[]> findQuotationSummaryByFilter(SearchForQuotation s) {
        StringBuilder sql = new StringBuilder(
                "select p.purchase_request_id, p.needed_by_date, p.priority, " +
                "count(q.quotation_id) as number_of_quotation, " +
                "min(q.total_amount) as est_price " +
                "from purchase_request p " +
                "left join quotation q on p.purchase_request_id = q.purchase_request_id " +
                "left join users u on p.creator_id = u.user_id " +
                "where p.status = 'APPROVED' "
        );

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
                    Date rawDate = rs.getDate("needed_by_date");
                    result.put(purchaseId, new Object[]{
                            rawDate != null ? rawDate.toLocalDate() : null,
                            rs.getString("priority"),
                            rs.getInt("number_of_quotation"),
                            rs.getBigDecimal("est_price")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing quotation summary query", e);
        }

        return result;
    }

}
