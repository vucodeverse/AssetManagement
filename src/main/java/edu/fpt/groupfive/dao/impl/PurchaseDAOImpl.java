package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseDAOImpl implements PurchaseDAO {

    private final DatabaseConfig databaseConfig;
    private final PurchaseDetailDAO purchaseDetailDAO;

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
                preparedStatement.setTimestamp(8,  Timestamp.valueOf(purchase.getCreatedAt().atStartOfDay()));
            } else {
                preparedStatement.setNull(8, Types.TIMESTAMP);
            }
            
            if (purchase.getUpdatedAt() != null) {
                preparedStatement.setTimestamp(9, Timestamp.valueOf(purchase.getUpdatedAt().atStartOfDay()));
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

    @Override
    public Optional<Purchase> findById(Integer purchaseId) {

        String sql = "select * from purchase_request where purchase_request_id = ?";

        try (Connection connection = databaseConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){

                //set data
                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("purchase_request_id"));
                purchase.setStatus(Request.valueOf(rs.getString("status")));
                purchase.setNote(rs.getString("note"));
                purchase.setRejectReason(rs.getString("reject_reason"));
                purchase.setCreatedByUser(rs.getInt("creator_id"));
                Date neededByDate = rs.getDate("needed_by_date");
                purchase.setNeededByDate(
                        neededByDate != null ? Date.valueOf(neededByDate.toLocalDate()) : null
                );

                purchase.setReason(rs.getString("request_reason"));
                purchase.setPriority(rs.getString("priority"));
                purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));

                Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
                if (approvedAt != null) {
                    purchase.setApprovedAt(approvedAt.toLocalDateTime());
                }

                purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
                purchase.setCreatedAt(rs.getDate("created_at").toLocalDate());
                Date updatedAt = rs.getDate("updated_at");
                purchase.setUpdatedAt(
                        updatedAt != null ? updatedAt.toLocalDate() : null
                );

                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId)
                );

                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Purchase> findByIdAndApproved(Integer purchaseId, String status) {
        String sql = "select * from purchase_request where purchase_request_id = ? and status = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            preparedStatement.setString(2, status);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){

                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("purchase_request_id"));
                purchase.setStatus(Request.valueOf(rs.getString("status")));
                purchase.setNote(rs.getString("note"));
                purchase.setRejectReason(rs.getString("reject_reason"));
                purchase.setCreatedByUser(rs.getInt("creator_id"));
                Date neededByDate = rs.getDate("needed_by_date");
                purchase.setNeededByDate(
                        neededByDate != null ? Date.valueOf(neededByDate.toLocalDate()) : null
                );

                purchase.setReason(rs.getString("request_reason"));
                purchase.setPriority(rs.getString("priority"));
                purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));

                Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
                if (approvedAt != null) {
                    purchase.setApprovedAt(approvedAt.toLocalDateTime());
                }

                purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));
                purchase.setCreatedAt(rs.getDate("created_at").toLocalDate());
                Date updatedAt = rs.getDate("updated_at");
                purchase.setUpdatedAt(
                        updatedAt != null ? updatedAt.toLocalDate() : null
                );

                purchase.setPurchaseDetails(
                        purchaseDetailDAO.findByPurchaseRequestId(purchaseId)
                );

                return Optional.of(purchase);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }
}
