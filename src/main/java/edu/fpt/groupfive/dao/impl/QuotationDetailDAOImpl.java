package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
             PreparedStatement preparedStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setInt(1, quotationDetail.getQuotationId());
            preparedStatement.setInt(2, quotationDetail.getPurchaseDetailId());

                 preparedStatement.setInt(3, quotationDetail.getAssetTypeId());
            
            preparedStatement.setInt(4, quotationDetail.getQuantity());
            preparedStatement.setString(5, quotationDetail.getQuotationDetailNote());
            preparedStatement.setInt(6, quotationDetail.getWarrantyMonths());
            preparedStatement.setBigDecimal(7, quotationDetail.getPrice());

             preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public Optional<QuotationDetail> findById(Integer quotationDetailId) {
        return Optional.empty();
    }

    @Override
    public List<QuotationDetail> findByPurchaseId(Integer purchaseId) {

        String sql = "select qd.* from quotation q join quotation_detail qd on q.quotation_id = qd.quotation_id where" +
                " q.purchase_request_id = ?";
        List<QuotationDetail> quotationDetails = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setInt(1, purchaseId);

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){

                QuotationDetail q = new QuotationDetail();
                q.setQuotationId(rs.getInt("quotation_id"));
                q.setId(rs.getInt("quotation_detail_id"));
                q.setPurchaseDetailId(rs.getInt("purchase_request_detail_id"));
                q.setAssetTypeId(rs.getInt("asset_type_id"));
                q.setQuantity(rs.getInt("quantity"));
                q.setQuotationDetailNote(rs.getString("quotation_detail_note"));
                q.setWarrantyMonths(rs.getInt("warranty_months"));
                q.setPrice(BigDecimal.valueOf(rs.getInt("price")));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return quotationDetails;
    }

    @Override
    public List<QuotationDetail> findByQuotationId(Integer quotationId) {

        String sql = "select * from quotation_detail where  quotation_id = ?";

        List<QuotationDetail> quotationDetails = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setInt(1, quotationId);

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){

                QuotationDetail q = new QuotationDetail();
                q.setQuotationId(rs.getInt("quotation_id"));
                q.setId(rs.getInt("quotation_detail_id"));
                q.setPurchaseDetailId(rs.getInt("purchase_request_detail_id"));
                q.setAssetTypeId(rs.getInt("asset_type_id"));
                q.setQuantity(rs.getInt("quantity"));
                q.setQuotationDetailNote(rs.getString("quotation_detail_note"));
                q.setWarrantyMonths(rs.getInt("warranty_months"));
                q.setPrice(BigDecimal.valueOf(rs.getInt("price")));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotationDetails;
    }

}
