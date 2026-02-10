package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseDetailDAOImpl implements PurchaseDetailDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(PurchaseDetail purchaseDetail) {
        String sql ="insert into purchase_request_detail (quantity, pr_id, asset_type_id, spec_requirement, note) " +
                "values (?, ?, ?,?, ?)";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
             preparedStatement.setInt(1,purchaseDetail.getQuantity());
             preparedStatement.setInt(2,purchaseDetail.getPurchaseRequestId());
             preparedStatement.setInt(3,purchaseDetail.getAssetTypeId());
             preparedStatement.setString(4,purchaseDetail.getSpecificationRequirement());
             preparedStatement.setString(5,purchaseDetail.getNote());
             preparedStatement.executeUpdate();
        }catch (Exception exception){
            throw new RuntimeException(exception);
        }

    }

    @Override
    public Optional<PurchaseDetail> findById(Integer purchaseDetailId) {
        return Optional.empty();
    }

    @Override
    public List<PurchaseDetail> findByPurchaseRequestId(Integer purchaseRequestId) {
        String sql = "select * from purchase_request_detail where purchase_request_id = ?";
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
                            rs.getString("spec_requirement")
                    );
                    detail.setNote(rs.getString("note"));
                    detail.setAssetTypeId(rs.getInt("asset_type_id"));
                    detail.setPurchaseRequestId(rs.getInt("purchase_request_id"));
                    detail.setPrice(rs.getBigDecimal("estimated_price"));

                    purchaseDetails.add(detail);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error finding PurchaseDetails by purchaseRequestId=" + purchaseRequestId, e
            );
        }

        return purchaseDetails;
    }
}
