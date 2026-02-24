package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j(topic = "ORDER-DAO")
public class OrderDAOImpl implements OrderDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Integer insert(Order order) {
        String sql = "insert into purchase_orders " +
                "(total_amount, note, status, created_at, supplier_id, quotation_id, updated_at) " +
                "values (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, order.getTotalAmount());
            ps.setString(2, order.getOrderNote());
            ps.setString(3, order.getOrderStatus().toString());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setInt(5, order.getSupplierId());
            ps.setInt(6, order.getQuotationId());
            ps.setDate(7, Date.valueOf(LocalDate.now()));

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            log.error("Failed to insert purchase order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to insert purchase order", e);
        }
        return null;
    }

    @Override
    public Map<Integer, Integer> getOrderedQtyByQuotationDetail(List<Integer> quotationDetailIds) {
        if (quotationDetailIds == null || quotationDetailIds.isEmpty()) {
            return new HashMap<>();
        }
        String placeholders = quotationDetailIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "select pod.quotation_detail_id, sum(pod.quantity) as total_qty " +
                "from purchase_orders po " +
                "join purchase_order_details pod on po.purchase_order_id = pod.purchase_order_id " +
                "where pod.quotation_detail_id in (" + placeholders + ") " +
                "group by pod.quotation_detail_id";

        Map<Integer, Integer> map = new HashMap<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < quotationDetailIds.size(); i++) {
                ps.setInt(i + 1, quotationDetailIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("quotation_detail_id"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            log.error("Failed to get ordered qty: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get ordered qty", e);
        }
        return map;
    }

    @Override
    public List<Order> searchAndFilter(OrderSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "select po.purchase_order_id, po.total_amount, po.note, po.status, " +
                "po.created_at, po.supplier_id, po.quotation_id, po.approved_by, " +
                "po.updated_at, po.updated_by, " +
                "q.purchase_request_id, " +
                "s.supplier_name " +
                "from purchase_orders po " +
                "join quotation q on po.quotation_id = q.quotation_id " +
                "join supplier s on po.supplier_id = s.supplier_id " +
                "where 1=1 "
        );

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
                sql.append("po.purchase_order_id = ? or ");
                params.add(Integer.parseInt(criteria.getKeyword()));
            }
            sql.append("s.supplier_name like ? ) ");
            params.add("%" + criteria.getKeyword() + "%");
        }

        sql.append("order by q.purchase_request_id, po.created_at");

        List<Order> orders = new ArrayList<>();
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
                order.setCreatedAt(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null);
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setQuotationId(rs.getInt("quotation_id"));
                order.setApprovedBy(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null);
                order.setUpdatedAt(rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null);
                order.setUpdateBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null);

                // extra fields read from JOIN — stored transiently via supplierName (carried via orderNote workaround)
                // passed to service via a side-channel map: key=orderId, value=[purchaseRequestId, supplierName]
                // We store them separately below
                int purchaseRequestId = rs.getInt("purchase_request_id");
                String supplierName = rs.getString("supplier_name");

                // We use purchaseOrderNote field (not in table) to carry supplierName to service
                order.setPurchaseOrderNote(supplierName + "|" + purchaseRequestId);

                orders.add(order);
            }

        } catch (SQLException e) {
            log.error("Failed to search purchase orders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search purchase orders", e);
        }
        return orders;
    }
}
