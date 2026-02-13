package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderDAOImpl implements OrderDAO
{

    private final DatabaseConfig databaseConfig;
    private final PriorityOrdered priorityOrdered;

    @Override
    public Integer insert(Order order) {

        String sql = "insert into purchase_orders (total_amount, note, status, created_at, supplier_id, quotation_id," +
                " approved_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?,?,?,?,?)";

        try(Connection connection = databaseConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setBigDecimal(1, order.getTotalAmount());
            preparedStatement.setString(2, order.getOrderNote());
            preparedStatement.setString(3, order.getOrderStatus().toString());
            preparedStatement.setTimestamp(4,Timestamp.valueOf(order.getCreatedAt().atStartOfDay()));
            preparedStatement.setInt(5, order.getSupplierId());
            preparedStatement.setInt(6, order.getQuotationId());
            preparedStatement.setInt(7, order.getApprovedBy());
            preparedStatement.setTimestamp(8, Timestamp.valueOf(order.getUpdatedAt().atStartOfDay()) != null ?
                    Timestamp.valueOf(order.getUpdatedAt().atStartOfDay()) : null);
            preparedStatement.setInt(9, order.getApprovedBy());

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            }
        }catch (SQLException e){

        }
        return 0;
    }

    @Override
    public Map<Integer, Integer> getOrderedQtyByQuotationDetail(List<Integer> quotationDetailId) {
        String placeHolder = quotationDetailId.stream().map(id -> "?").collect(Collectors.joining(","));

        String sql = "select pod.quotation_detail_id, sum(pod.quantity) as qty_pod from purchase_orders po join purchase_order_details pod\n" +
                "    on po\n" +
                "    .purchase_order_id = pod.purchase_order_id where pod.quotation_detail_id in ("+placeHolder+" ) group by pod\n" +
                "        .quotation_detail_id\n";
        Map<Integer, Integer> map = new HashMap<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // set từng id vào ?
            for (int i = 0; i < quotationDetailId.size(); i++) {
                ps.setInt(i + 1, quotationDetailId.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Integer id = rs.getInt("quotation_detail_id");
                Integer qty = rs.getInt("qty_pod");

                map.put(id, qty);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return map;
    }
}
