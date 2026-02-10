package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@RequiredArgsConstructor
public class QuotationDetailDAOImpl implements QuotationDetailDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Integer insert(QuotationDetail quotationDetail) {
        String sql = "insert into quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, " +
                "quantity," +
                "quotation_detail_note, warranty_months, price) values (?,?,?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setInt(1, quotationDetail.getQuotationId());
            preparedStatement.setInt(2, quotationDetail.getPurchaseDetailId());
            
            if (quotationDetail.getAssetType() != null) {
                 preparedStatement.setInt(3, quotationDetail.getAssetType().getTypeId());
            } else {
                 preparedStatement.setNull(3, Types.INTEGER);
            }
            
            preparedStatement.setInt(4, quotationDetail.getQuantity());
            preparedStatement.setString(5, quotationDetail.getQuotationDetailNote());
            preparedStatement.setInt(6, quotationDetail.getWarrantyMonths());
            preparedStatement.setBigDecimal(7, quotationDetail.getPrice());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
