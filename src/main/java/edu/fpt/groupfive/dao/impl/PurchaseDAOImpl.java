package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseDAOImpl implements PurchaseDAO {

    private final DatabaseConfig databaseConfig;
    private final PurchaseDetailDAO purchaseDetailDAO;

    @Override
    public int insert(Purchase purchase) {


        String sql = "insert into purchase_request (status, note, creator_id, needed_by_date, priority, " +
                "approved_by_director_id,reject_reason, created_at, updated_at, request_reason, " +
                "approved_by_director_at," +
                "purchase_staff_user_id) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setString(1, purchase.getStatus() != null ? purchase.getStatus().toString() : null);
            preparedStatement.setString(2, purchase.getNote());
            preparedStatement.setInt(3, purchase.getCreatedByUser());
            
                preparedStatement.setDate(4, new Date(purchase.getNeededByDate().getTime()));
            preparedStatement.setString(5, purchase.getPriority());
            

                preparedStatement.setObject(6, purchase.getApprovedByDirector());

            preparedStatement.setString(7, purchase.getRejectReason());

                preparedStatement.setTimestamp(8,  Timestamp.valueOf(purchase.getCreatedAt().atStartOfDay()));

                preparedStatement.setTimestamp(9,
                        purchase.getUpdatedAt() != null ? Timestamp.valueOf(purchase.getUpdatedAt().atStartOfDay()):
                                null);

            preparedStatement.setString(10, purchase.getReason());
                preparedStatement.setTimestamp(11,
                        purchase.getApprovedAt() != null ?  Timestamp.valueOf(purchase.getApprovedAt()) : null);
                preparedStatement.setObject(12, purchase.getPurchaseStaffId());

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

    @Override
    public List<Purchase> findAll() {

        String sql ="select * from purchase_request";

        List<Purchase> purchases = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
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
                purchase.setCreatedAt(rs.getDate("created_at").toLocalDate());
                purchase.setApprovedByDirector(rs.getInt("approved_by_director_id"));
                Timestamp approvedAt = rs.getTimestamp("approved_by_director_at");
                if (approvedAt != null) {
                    purchase.setApprovedAt(approvedAt.toLocalDateTime());
                }
                purchase.setPurchaseStaffId(rs.getInt("purchase_staff_user_id"));

                Date updatedAt = rs.getDate("updated_at");
                purchase.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDate() : null);
                purchase.setPurchaseDetails(purchaseDetailDAO.findByPurchaseRequestId(purchase.getId()));
                purchases.add(purchase);
            }

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return purchases;
    }


}
