
package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.OrderDetailDAO;
import edu.fpt.groupfive.model.OrderDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@RequiredArgsConstructor
public class OrderDetailDAOImpl implements OrderDetailDAO
{

    private final DatabaseConfig databaseConfig;


    @Override
    public Integer insetOrderDetail(OrderDetail orderDetail, Integer orderId) {
        String sql ="insert into purchase_order_details " +
                "(quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, discount, note, quotation_detail_id, expected_delivery_date) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderDetail.getQuantity());
            ps.setBigDecimal(2, orderDetail.getPrice());
            ps.setBigDecimal(3, orderDetail.getTaxRate());
            ps.setObject(4, orderId);

            ps.setObject(5, orderDetail.getAssetTypeId());
            ps.setBigDecimal(6, orderDetail.getDiscountRate());
            ps.setString(7, orderDetail.getOrderDetailNote());
            ps.setObject(8, orderDetail.getQuotationDetailId());
            ps.setDate(9, orderDetail.getExpectedDeliveryDate() != null ? Date.valueOf(orderDetail.getExpectedDeliveryDate()) : null);


            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        }catch (SQLException e){
            throw new RuntimeException("Failed to insert order detail", e);
        }

        return null;
    }

    @Override
    public java.util.List<OrderDetail> findByOrderId(Integer orderId) {
        String sql = "select purchase_order_detail_id, quantity, unit_price, tax_rate, " +
                "discount, note, asset_type_id, quotation_detail_id, expected_delivery_date " +
                "from purchase_order_details " +
                "where purchase_order_id = ?";

        java.util.List<OrderDetail> results = new java.util.ArrayList<>();
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
                detail.setExpectedDeliveryDate(rs.getDate("expected_delivery_date") != null ? rs.getDate("expected_delivery_date").toLocalDate() : null);

                results.add(detail);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order details", e);
        }
        return results;
    }
}
