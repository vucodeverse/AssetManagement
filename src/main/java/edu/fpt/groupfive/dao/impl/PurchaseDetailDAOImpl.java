package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseDetailDAOImpl implements PurchaseDetailDAO {

    private final DatabaseConfig databaseConfig;

    @Value("${dao.common.insert_error}")
    private String insertErrorMsg;

    // insert purchase detail
    @Override
    public void insert(PurchaseDetail purchaseDetail, Connection connection) {
        String sql = "insert into purchase_request_detail (estimated_price, quantity, purchase_request_id, " +
                "asset_type_id, spec_requirement, note) values (?, ?, ?,?, ?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBigDecimal(1, purchaseDetail.getEstimatePrice());
            preparedStatement.setInt(2, purchaseDetail.getQuantity());
            preparedStatement.setInt(3, purchaseDetail.getPurchaseRequestId());
            preparedStatement.setInt(4, purchaseDetail.getTypeId());
            preparedStatement.setString(5, purchaseDetail.getSpecificationRequirement());
            preparedStatement.setString(6, purchaseDetail.getPurchaseDetailNote());
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            throw new DataAccessException(insertErrorMsg, exception);
        }
    }

    // tìm purchase detail theo purchse request
    @Override
    public List<PurchaseDetail> findByPurchaseRequestId(Integer purchaseRequestId) {
        String sql = "select pd.*, at.type_name " +
                "from purchase_request_detail pd " +
                "left join asset_type at on pd.asset_type_id = at.asset_type_id " +
                "where pd.purchase_request_id = ?";
        List<PurchaseDetail> purchaseDetails = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, purchaseRequestId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseDetail detail = new PurchaseDetail();

                    // set gia tri co tung field
                    detail.setId(rs.getInt("purchase_request_detail_id"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setSpecificationRequirement(
                            rs.getString("spec_requirement"));
                    detail.setPurchaseDetailNote(rs.getString("note"));
                    detail.setTypeId(rs.getInt("asset_type_id"));
                    detail.setPurchaseRequestId(rs.getInt("purchase_request_id"));
                    detail.setEstimatePrice(rs.getBigDecimal("estimated_price"));

                    purchaseDetails.add(detail);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(insertErrorMsg, e);
        }

        return purchaseDetails;
    }

    // xóa tất cả purchase detail theo purchase request id (dùng khi update draft)
    @Override
    public void deleteByPurchaseRequestId(Integer purchaseRequestId, Connection conn) {
        String sql = "delete from purchase_request_detail where purchase_request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, purchaseRequestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(insertErrorMsg, e);
        }
    }
}