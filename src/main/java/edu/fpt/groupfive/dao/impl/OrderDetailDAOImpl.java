
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
    public Integer insetOrderDetail(OrderDetail orderDetail) {
        String sql ="insert into purchase_order_details (quantity, unit_price, tax_rate, asset_type_id, discount, " +
                "note, quotation_detail_id) values (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderDetail.getQuantity());
            ps.setBigDecimal(2, orderDetail.getPrice());
            ps.setBigDecimal(3, orderDetail.getTaxRate());
            ps.setInt(4, orderDetail.getAssetTypeId());
            ps.setBigDecimal(5, orderDetail.getDiscountRate());
            ps.setString(6, orderDetail.getOrderDetailNote());
            ps.setInt(7, orderDetail.getQuotationDetailId());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        }catch (SQLException ex){

        }

        return 0;
    }
}
