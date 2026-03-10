package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.OrderDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDate;
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
                "(order_date, total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by, updated_at, updated_by, warehouse_id) "
                +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setDate(1, order.getCreatedAt() != null ? Date.valueOf(order.getCreatedAt())
                        : Date.valueOf(LocalDate.now()));
                ps.setBigDecimal(2, order.getTotalAmount());
                ps.setString(3, order.getOrderNote());
                ps.setString(4, order.getOrderStatus() != null ? order.getOrderStatus().toString()
                        : OrderStatus.PENDING.toString());
                ps.setDate(5, Date.valueOf(LocalDate.now()));
                ps.setObject(6, order.getPurchaseRequestId());
                ps.setObject(7, order.getSupplierId());
                ps.setObject(8, order.getQuotationId());
                ps.setObject(9, order.getApprovedBy());
                ps.setDate(10, Date.valueOf(LocalDate.now()));
                ps.setObject(11, order.getUpdatedBy());
                ps.setObject(12, order.getWarehouseId());

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
    public Map<Integer, Integer> getOrderedQtyByPurchaseDetail(List<Integer> purchaseDetailIds) {
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
    public List<Object[]> searchAndFilter(PurchaseOrderSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select po.purchase_order_id, po.order_date, po.total_amount, po.note, po.status, " +
                        "po.created_at, po.purchase_request_id, po.supplier_id, po.quotation_id, po.approved_by, " +
                        "po.updated_at, po.updated_by, po.warehouse_id, " +
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
                order.setCreatedAt(rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate()
                        : (rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null));
                order.setPurchaseRequestId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                order.setWarehouseId(rs.getObject("warehouse_id") != null ? rs.getInt("warehouse_id") : null);

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
        String sql = "select purchase_order_id, order_date, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by, warehouse_id " +
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
                order.setCreatedAt(rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate()
                        : (rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null));
                order.setPurchaseRequestId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                order.setWarehouseId(rs.getObject("warehouse_id") != null ? rs.getInt("warehouse_id") : null);

                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find purchase order", e);
        }
        return java.util.Optional.empty();
    }

    @Override
    public long countAll() {
        String sql = "select count(*) from purchase_orders";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public java.math.BigDecimal sumTotalAmount() {
        String sql = "select coalesce(sum(total_amount), 0) from purchase_orders";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return java.math.BigDecimal.ZERO;
    }

    // lấy ra các po gần đây theo giới hạn
    @Override
    public List<Order> findRecent() {
        String sql = "select purchase_order_id, order_date, total_amount, note, status, " +
                "created_at, purchase_request_id, supplier_id, quotation_id, approved_by, " +
                "updated_at, updated_by, warehouse_id " +
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
                order.setCreatedAt(rs.getDate("order_date") != null ? rs.getDate("order_date").toLocalDate()
                        : (rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null));
                order.setPurchaseRequestId(rs.getInt("purchase_request_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                order.setUpdatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);
                order.setWarehouseId(rs.getObject("warehouse_id") != null ? rs.getInt("warehouse_id") : null);
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recent purchase orders", e);
        }
        return orders;
    }

    @Override
    public Integer getWhIdFromPr(Integer purchaseId) {

        String sql =  "select o.warehouse_id from purchase_orders o  where o.purchase_request_id = ?";
        try (Connection connection = databaseConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setInt(1,  purchaseId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) return (Integer) rs.getObject("warehouse_id");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
