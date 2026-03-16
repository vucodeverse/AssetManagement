package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.OrderDetailDAO;
import edu.fpt.groupfive.model.OrderDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderDetailDAOImpl implements OrderDetailDAO {

    private final DatabaseConfig databaseConfig;

    // insert po detail
    @Override
    public Integer insert(OrderDetail orderDetail, Integer orderId, Connection connection) {
        String sql = "insert into purchase_order_details " +
                "(quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, discount, note, quotation_detail_id, delivery_date) "
                +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderDetail.getQuantity());
            ps.setBigDecimal(2, orderDetail.getPrice());
            ps.setBigDecimal(3, orderDetail.getTaxRate());
            ps.setInt(4, orderId);

            ps.setObject(5, orderDetail.getAssetTypeId());
            ps.setBigDecimal(6, orderDetail.getDiscountRate());
            ps.setString(7, orderDetail.getOrderDetailNote());
            ps.setObject(8, orderDetail.getQuotationDetailId());
            ps.setDate(9,
                    orderDetail.getDeliveryDate() != null ? Date.valueOf(orderDetail.getDeliveryDate())
                            : null);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order detail", e);
        }

        return null;
    }

    // lấy  ra list detail theo po
    @Override
    public List<OrderDetail> findByOrderId(Integer orderId) {
        String sql = "select purchase_order_detail_id, quantity, unit_price, tax_rate, " +
                "discount, note, asset_type_id, quotation_detail_id, delivery_date " +
                "from purchase_order_details " +
                "where purchase_order_id = ?";

        List<OrderDetail> results = new java.util.ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setId(rs.getInt("purchase_order_detail_id"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setPrice(rs.getBigDecimal("unit_price"));
                detail.setTaxRate(rs.getBigDecimal("tax_rate"));
                detail.setDiscountRate(rs.getBigDecimal("discount"));
                detail.setOrderDetailNote(rs.getString("note"));
                detail.setAssetTypeId(rs.getInt("asset_type_id"));
                detail.setQuotationDetailId(rs.getInt("quotation_detail_id"));
                detail.setDeliveryDate(rs.getDate("delivery_date") != null
                        ? rs.getDate("delivery_date").toLocalDate()
                        : null);

                results.add(detail);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order details", e);
        }
        return results;
    }

    @Override
    public void updateDeliveryDate(Integer orderId, LocalDate deliveryDate) {
        String sql = "update purchase_order_details set delivery_date = ? where purchase_order_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(deliveryDate));
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update delivery date", e);
        }
    }

    @Override
    public List<OrderDetail> findAll() {
        String sql = "select * from purchase_order_details";
        List<OrderDetail> results = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setId(rs.getInt("purchase_order_detail_id"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setPrice(rs.getBigDecimal("unit_price"));
                detail.setTaxRate(rs.getBigDecimal("tax_rate"));
                detail.setDiscountRate(rs.getBigDecimal("discount"));
                detail.setOrderDetailNote(rs.getString("note"));
                detail.setAssetTypeId(rs.getInt("asset_type_id"));
                detail.setQuotationDetailId(rs.getInt("quotation_detail_id"));
                detail.setDeliveryDate(rs.getDate("delivery_date") != null
                        ? rs.getDate("delivery_date").toLocalDate()
                        : null);

                results.add(detail);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  results;
    }
}
