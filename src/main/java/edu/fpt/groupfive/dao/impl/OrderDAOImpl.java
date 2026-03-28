package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.OrderDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.model.OrderDetail;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderDAOImpl implements OrderDAO {

    private final DatabaseConfig databaseConfig;
    private final OrderDetailDAO orderDetailDAO;

    @Value("${dao.common.insert_error}")
    private String insertErrorMsg;
    @Value("${order.create.excess_quantity_basic}")
    private String excessQuantityMsg;

    @Value("${dao.order.generate_id_error}")
    private String generateIdErrorMsg;

    @Value("${dao.common.find_error}")
    private String findErrorMsg;

    @org.springframework.beans.factory.annotation.Value("${dao.order.detail.find_error}")
    private String findOrderDetailErrorMsg;

    @Override
    public Integer insert(Order order) {
        String sql = "insert into purchase_orders " +
                "(total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by, updated_at, updated_by) "
                +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            // tạm khóa yeee cầu mua sắm chi tiết và số lượng yêu cầu mua ko cho các thread
            // khác thay đổi
            String lockSql = "SELECT purchase_request_detail_id, quantity FROM purchase_request_detail WITH (UPDLOCK, HOLDLOCK) WHERE purchase_request_id = ?";

            // lưu lại id yc detail và số lượng của nó
            Map<Integer, Integer> prdMaxQty = new HashMap<>();
            try (PreparedStatement psLock = connection.prepareStatement(lockSql)) {
                psLock.setInt(1, order.getPurchaseId());
                try (ResultSet rsLock = psLock.executeQuery()) {
                    while (rsLock.next()) {
                        prdMaxQty.put(rsLock.getInt("purchase_request_detail_id"), rsLock.getInt("quantity"));
                    }
                }
            }

            // lấy ra quotation detail và purchase request detail
            String mappingSql = "SELECT quotation_detail_id, purchase_request_detail_id FROM quotation_detail WHERE quotation_id = ?";

            // lưu lại qd và prd
            Map<Integer, Integer> qdToPrd = new HashMap<>();
            try (PreparedStatement psMap = connection.prepareStatement(mappingSql)) {
                psMap.setInt(1, order.getQuotationId());
                try (ResultSet rsMap = psMap.executeQuery()) {
                    while (rsMap.next()) {
                        qdToPrd.put(rsMap.getInt("quotation_detail_id"), rsMap.getInt("purchase_request_detail_id"));
                    }
                }
            }

            //
            if (!prdMaxQty.isEmpty()) {
                String placeholders = prdMaxQty.keySet().stream().map(id -> "?").collect(Collectors.joining(", "));

                // lấy ra tổng số lượng đc báo giá của từng request detail
                String orderedSql = "select qd.purchase_request_detail_id, sum(pod.quantity) as total_qty " +
                        "from purchase_order_details pod " +
                        "join quotation_detail qd on pod.quotation_detail_id = qd.quotation_detail_id " +
                        "join purchase_orders po on pod.purchase_order_id = po.purchase_order_id " +
                        "where qd.purchase_request_detail_id in (" + placeholders + ") " +
                        "and po.status <> 'DELETED' " +
                        "group by qd.purchase_request_detail_id";

                // lưu prd và tổng số lượng đã order
                Map<Integer, Integer> prdOrderedQty = new HashMap<>();
                try (PreparedStatement psOrdered = connection.prepareStatement(orderedSql)) {
                    int i = 1;
                    for (Integer prdId : prdMaxQty.keySet()) {
                        psOrdered.setInt(i++, prdId);
                    }
                    try (ResultSet rsOrdered = psOrdered.executeQuery()) {
                        while (rsOrdered.next()) {
                            prdOrderedQty.put(rsOrdered.getInt("purchase_request_detail_id"),
                                    rsOrdered.getInt("total_qty"));
                        }
                    }
                }

                if (order.getOrderDetails() != null) {

                    // lưu id detail và số lượng sẽ nhập ở thread hiện tại
                    Map<Integer, Integer> currentRequestQty = new HashMap<>();
                    for (OrderDetail od : order.getOrderDetails()) {

                        // lấy ra prdId của qd hiện tiện
                        Integer prdId = qdToPrd.get(od.getQuotationDetailId());
                        if (prdId != null) {

                            // số lượng tối đa có thể mua
                            int max = prdMaxQty.getOrDefault(prdId, 0);

                            // số lượng đã mua
                            int already = prdOrderedQty.getOrDefault(prdId, 0);

                            // số lượng mua trong thread hiện tại
                            int inThisRequest = currentRequestQty.getOrDefault(prdId, 0);

                            if (already + inThisRequest + od.getQuantity() > max) {
                                throw new InvalidDataException(
                                        excessQuantityMsg + " (Cần thêm: "
                                                + od.getQuantity() + ", Đã đặt trong các đơn cũ: " + already
                                                + ", Đã có trong đơn này: " + inThisRequest + ", Tối đa: " + max + ")");
                            }

                            // nếu hợp lệ thì update số lượng đã mua của prd này
                            currentRequestQty.put(prdId, inThisRequest + od.getQuantity());

                        }
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setBigDecimal(1, order.getTotalAmount());
                ps.setString(2, order.getOrderNote());
                ps.setString(3, order.getOrderStatus() != null ? order.getOrderStatus().name()
                        : PurchaseProcessStatus.PENDING.name());
                ps.setTimestamp(4, order.getCreatedAt() != null ? Timestamp.valueOf(order.getCreatedAt())
                        : Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(5, order.getPurchaseId());
                ps.setObject(6, order.getSupplierId());
                ps.setObject(7, order.getQuotationId());
                ps.setObject(8, order.getApprovedBy());
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(10, order.getUpdatedBy());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException(generateIdErrorMsg);
                    }
                    int orderId = rs.getInt(1);

                    if (order.getOrderDetails() != null) {
                        for (var detail : order.getOrderDetails()) {
                            orderDetailDAO.insert(detail, orderId, connection);
                        }
                    }

                    connection.commit();
                    return orderId;
                }
            }

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception ignored) {
                }
            }
            if (e instanceof InvalidDataException) {
                throw (InvalidDataException) e;
            }
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    // lấy ra số lượng đã order ứng với từng purchas id
    @Override
    public Map<Integer, Integer> getOrderedQuantityByPurchaseDetailId(List<PurchaseDetail> purchaseDetailIds) {
        if (purchaseDetailIds == null || purchaseDetailIds.isEmpty()) {
            return new HashMap<>();
        }
        String placeholders = purchaseDetailIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "select qd.purchase_request_detail_id, sum(pod.quantity) as total_qty " +
                "from purchase_order_details pod " +
                "join quotation_detail qd on pod.quotation_detail_id = qd.quotation_detail_id " +
                "where qd.purchase_request_detail_id in (" + placeholders + ") " +
                "group by qd.purchase_request_detail_id";

        Map<Integer, Integer> map = new HashMap<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < purchaseDetailIds.size(); i++) {
                ps.setInt(i + 1, purchaseDetailIds.get(i).getId());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("purchase_request_detail_id"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(findOrderDetailErrorMsg, e);
        }
        return map;
    }

    @Override
    public Map<Integer, Integer> getReceivedQuantityByPurchaseDetailId(List<PurchaseDetail> purchaseDetailIds) {
        if (purchaseDetailIds == null || purchaseDetailIds.isEmpty()) {
            return new HashMap<>();
        }
        String placeholders = purchaseDetailIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "select qd.purchase_request_detail_id, sum(pod.received_quantity) as total_qty " +
                "from purchase_order_details pod " +
                "join quotation_detail qd on pod.quotation_detail_id = qd.quotation_detail_id " +
                "join purchase_orders po on pod.purchase_order_id = po.purchase_order_id " +
                "where qd.purchase_request_detail_id in (" + placeholders + ") " +
                "and po.status <> 'DELETED' " +
                "group by qd.purchase_request_detail_id";

        Map<Integer, Integer> map = new HashMap<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < purchaseDetailIds.size(); i++) {
                ps.setInt(i + 1, purchaseDetailIds.get(i).getId());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("purchase_request_detail_id"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(findOrderDetailErrorMsg, e);
        }
        return map;
    }

    // search cho màn purchase orderlisst
    @Override
    public List<Object[]> search(PurchaseOrderSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select po.purchase_order_id, po.total_amount, po.note, po.status, " +
                        "po.created_at, q.purchase_request_id, po.supplier_id, po.quotation_id, po.approved_by, " +
                        "po.updated_at, po.updated_by, " +
                        "s.supplier_name " +
                        "from purchase_orders po " +
                        "join supplier s on po.supplier_id = s.supplier_id join quotation q on q.quotation_id = po.quotation_id " +
                        "where po.status <> 'DELETED' ");

        List<Object> params = new ArrayList<>();

        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            sql.append("and po.status = ? ");
            params.add(criteria.getStatus());
        }

        if (criteria.getPurchaseId() != null) {
            sql.append("and q.purchase_request_id = ? ");
            params.add(criteria.getPurchaseId());
        }

        if (criteria.getSupplierName() != null && !criteria.getSupplierName().isBlank()) {
            sql.append("and s.supplier_name = ? ");
            params.add(criteria.getSupplierName());
        }

        if (criteria.getMinAmount() != null) {
            sql.append("and po.total_amount >= ? ");
            params.add(criteria.getMinAmount());
        }

        if (criteria.getMaxAmount() != null) {
            sql.append("and po.total_amount <= ? ");
            params.add(criteria.getMaxAmount());
        }

        if (criteria.getDateFrom() != null) {
            sql.append("and cast(po.created_at as date) >= ? ");
            params.add(Date.valueOf(criteria.getDateFrom()));
        }

        if (criteria.getDateTo() != null) {
            sql.append("and cast(po.created_at as date) <= ? ");
            params.add(Date.valueOf(criteria.getDateTo()));
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            sql.append("and ( ");
            if (criteria.getKeyword().matches("\\d+")) {
                sql.append("po.purchase_order_id = ? or q.purchase_request_id = ? or ");
                params.add(Integer.parseInt(criteria.getKeyword()));
                params.add(Integer.parseInt(criteria.getKeyword()));
            }
            sql.append("s.supplier_name like ? ) ");
            params.add("%" + criteria.getKeyword() + "%");
        }
        List<Object[]> results = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(PurchaseProcessStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setPurchaseId(rs.getInt("purchase_request_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(
                        rs.getDate("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);

                String supplierName = rs.getString("supplier_name");
                results.add(new Object[] { order, supplierName });
            }

        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return results;
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        String sql = "select purchase_order_id, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by " +
                "from purchase_orders " +
                "where purchase_order_id = ? and status <> 'DELETED'";

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(PurchaseProcessStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setPurchaseId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(
                        rs.getDate("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);

                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return Optional.empty();
    }

    // lấy ra các po
    @Override
    public List<Order> findRecent() {
        String sql = "select purchase_order_id, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by " +
                "from purchase_orders " +
                "where status <> 'DELETED' ";

        List<Order> orders = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(PurchaseProcessStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setPurchaseId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(
                        rs.getDate("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(findErrorMsg, e);
        }
        return orders;
    }

    @Override
    public void updateStatus(Integer orderId, PurchaseProcessStatus orderStatus) {
        String sql = "update purchase_orders set status = ?, updated_at = ? where purchase_order_id = ?";

        try (Connection connection = databaseConfig.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, orderStatus.name());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setInt(3, orderId);
                preparedStatement.executeUpdate(); // MISSING BEFORE
                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException(insertErrorMsg, e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new DataAccessException("Update thất bại", e);
        }
    }

    @Override
    public void updateUpdatedAt(Integer orderId) {
        String sql = "update purchase_orders set updated_at = ? where purchase_order_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setInt(2, orderId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Update updated_at thất bại", e);
        }
    }

}
