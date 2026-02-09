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

            preparedStatement.setInt(1, quotationDetail.getQuotation().getId());
            preparedStatement.setInt(2, quotationDetail.getPurchaseDetail().getId());
            preparedStatement.setInt(3, quotationDetail.getAssetType().getTypeId());
            preparedStatement.setInt(4, quotationDetail.getQuantity());
            preparedStatement.setInt(5,quotationDetail.getWarrantyMonths());

            preparedStatement.setBigDecimal(6, quotationDetail.getPrice());
            preparedStatement.setBigDecimal(7, quotationDetail.getTaxRate());
            preparedStatement.setBigDecimal(8, quotationDetail.getDiscountRate());
            preparedStatement.setString(9, quotationDetail.getQuotationDetailNote());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
