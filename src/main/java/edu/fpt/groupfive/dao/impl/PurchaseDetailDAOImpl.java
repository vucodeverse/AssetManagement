package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

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
}
