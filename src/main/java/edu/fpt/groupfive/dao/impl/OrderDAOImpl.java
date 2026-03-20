package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.OrderDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
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

    @Override
    public Integer insert(Order order) {
        String sql = "insert into purchase_orders " +
                "(order_date, total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by, updated_at, updated_by) "
                +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setTimestamp(1, order.getCreatedAt() != null ? Timestamp.valueOf(order.getCreatedAt())
                        : Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(2, order.getTotalAmount());
                ps.setString(3, order.getOrderNote());
                ps.setString(4, order.getOrderStatus() != null ? order.getOrderStatus().toString()
                        : OrderStatus.PENDING.toString());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(6, order.getPurchaseId());
                ps.setObject(7, order.getSupplierId());
                ps.setObject(8, order.getQuotationId());
                ps.setObject(9, order.getApprovedBy());
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(11, order.getUpdatedBy());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Failed to get generated purchase_order_id");
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

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception ignored) {
                }
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
    public Map<Integer, Integer> getOrderedQuantityByPurchaseDetailId(List<Integer> purchaseDetailIds) {
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
                ps.setInt(i + 1, purchaseDetailIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("purchase_request_detail_id"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get ordered qty by purchase detail", e);
        }
        return map;
    }

    // search cho màn purchase orderlisst
    @Override
    public List<Object[]> search(PurchaseOrderSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select po.purchase_order_id, po.total_amount, po.note, po.status, " +
                        "po.created_at, po.purchase_request_id, po.supplier_id, po.quotation_id, po.approved_by, " +
                        "po.updated_at, po.updated_by, " +
                        "s.supplier_name " +
                        "from purchase_orders po " +
                        "join supplier s on po.supplier_id = s.supplier_id " +
                        "where 1=1 ");

        List<Object> params = new ArrayList<>();

        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            sql.append("and po.status = ? ");
            params.add(criteria.getStatus());
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
                sql.append("po.purchase_order_id = ? or po.purchase_request_id = ? or ");
                params.add(Integer.parseInt(criteria.getKeyword()));
                params.add(Integer.parseInt(criteria.getKeyword()));
            }
            sql.append("s.supplier_name like ? ) ");
            params.add("%" + criteria.getKeyword() + "%");
        }

        sql.append("order by po.purchase_request_id, po.created_at");

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
                order.setOrderStatus(OrderStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                order.setPurchaseId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(
                        rs.getDate("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                String supplierName = rs.getString("supplier_name");
                results.add(new Object[] { order, supplierName });
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search purchase orders", e);
        }
        return results;
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        String sql = "select purchase_order_id, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by " +
                "from purchase_orders " +
                "where purchase_order_id = ?";

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(OrderStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
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
            throw new RuntimeException("Failed to find purchase order", e);
        }
        return java.util.Optional.empty();
    }

    // lấy ra các po gần đây theo giới hạn
    @Override
    public List<Order> findRecent() {
        String sql = "select purchase_order_id, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by " +
                "from purchase_orders " +
                "order by created_at desc";

        List<Order> orders = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(OrderStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
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
            throw new RuntimeException("Failed to find recent purchase orders", e);
        }
        return orders;
    }

    @Override
    public void updateStatus(Integer orderId, OrderStatus status) {
        String sql = "update purchase_orders set status = ?, updated_at = ? where purchase_order_id = ?";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toString());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update purchase order status", e);
        }
    }

    @Override
    public List<Object[]> findByStatus(OrderStatus status) {
        String sql = "select po.purchase_order_id, po.total_amount, po.note, po.status, " +
                "po.created_at, po.purchase_request_id, po.supplier_id, po.quotation_id, po.approved_by, " +
                "po.updated_at, po.updated_by, " +
                "s.supplier_name " +
                "from purchase_orders po " +
                "join supplier s on po.supplier_id = s.supplier_id " +
                "where po.status = ? " +
                "order by po.created_at desc";

        List<Object[]> results = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("purchase_order_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setOrderNote(rs.getString("note"));
                order.setOrderStatus(OrderStatus.valueOf(rs.getString("status").toUpperCase()));
                order.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                order.setPurchaseId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(
                        rs.getDate("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                String supplierName = rs.getString("supplier_name");
                results.add(new Object[] { order, supplierName });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find purchase orders by status", e);
        }
        return results;
    }
}
