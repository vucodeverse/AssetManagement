package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@RequiredArgsConstructor
public class PurchaseDAOImpl implements PurchaseDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public int insert(Purchase purchase) {


        String sql = "insert into purchase_request (status, note, creator_id, needed_by_date, priority, approved_by_director_id,reject_reason, created_at, updated_at, reason, approved_at,purchase_staff_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setString(1, purchase.getStatus().toString());
            preparedStatement.setString(2, purchase.getNote());
            preparedStatement.setInt(3, purchase.getCreatedByUser());
            preparedStatement.setDate(4, Date.valueOf(purchase.getNeededByDate().toString()));
            preparedStatement.setString(5, purchase.getPriority());
            preparedStatement.setInt(6, purchase.getApprovedByDirector());
            preparedStatement.setString(7, purchase.getRejectReason());
            preparedStatement.setDate(8,  Date.valueOf(purchase.getCreatedAt().toString()));
            preparedStatement.setDate(9,  Date.valueOf(purchase.getUpdatedAt().toString()));
            preparedStatement.setString(10, purchase.getReason());
            preparedStatement.setDate(11, Date.valueOf(purchase.getApprovedAt().toString()));
            preparedStatement.setInt(12, purchase.getPurchaseStaffId());

            preparedStatement.executeQuery();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);

        }catch (Exception exception){
            throw new RuntimeException(exception);
        }

        return 0;
    }
}
