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

            preparedStatement.setString(1, purchase.getStatus() != null ? purchase.getStatus().toString() : null);
            preparedStatement.setString(2, purchase.getNote());
            preparedStatement.setInt(3, purchase.getCreatedByUser());
            
            if (purchase.getNeededByDate() != null) {
                preparedStatement.setDate(4, new java.sql.Date(purchase.getNeededByDate().getTime()));
            } else {
                preparedStatement.setNull(4, Types.DATE);
            }
            
            preparedStatement.setString(5, purchase.getPriority());
            
            if (purchase.getApprovedByDirector() != null) {
                preparedStatement.setInt(6, purchase.getApprovedByDirector());
            } else {
                preparedStatement.setNull(6, Types.INTEGER);
            }

            preparedStatement.setString(7, purchase.getRejectReason());
            
            if (purchase.getCreatedAt() != null) {
                preparedStatement.setTimestamp(8,  new java.sql.Timestamp(purchase.getCreatedAt().getTime()));
            } else {
                preparedStatement.setNull(8, Types.TIMESTAMP);
            }
            
            if (purchase.getUpdatedAt() != null) {
                preparedStatement.setTimestamp(9,  new java.sql.Timestamp(purchase.getUpdatedAt().getTime()));
            } else {
                preparedStatement.setNull(9, Types.TIMESTAMP);
            }

            preparedStatement.setString(10, purchase.getReason());

            if (purchase.getApprovedAt() != null) {
                preparedStatement.setTimestamp(11, Timestamp.valueOf(purchase.getApprovedAt()));
            } else {
                preparedStatement.setNull(11, Types.TIMESTAMP);
            }
            
            if (purchase.getPurchaseStaffId() != null) {
                preparedStatement.setInt(12, purchase.getPurchaseStaffId());
            } else {
                preparedStatement.setNull(12, Types.INTEGER);
            }

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);

        }catch (Exception exception){
            throw new RuntimeException(exception);
        }

        return 0;
    }
}
