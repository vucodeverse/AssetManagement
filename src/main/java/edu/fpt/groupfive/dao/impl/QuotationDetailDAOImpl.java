package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
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
                "quotation_detail_note, warranty_months, price, tax_rate, discount_rate, rejected_reason) values (?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setObject(1, quotationDetail.getQuotationId());
            preparedStatement.setObject(2, quotationDetail.getPurchaseDetailId());
            preparedStatement.setObject(3, quotationDetail.getAssetTypeId());

            preparedStatement.setInt(4, quotationDetail.getQuantity() != null ? quotationDetail.getQuantity() : 0);
            preparedStatement.setString(5, quotationDetail.getQuotationDetailNote());
            preparedStatement.setInt(6,
                    quotationDetail.getWarrantyMonths() != null ? quotationDetail.getWarrantyMonths() : 0);
            preparedStatement.setBigDecimal(7, quotationDetail.getPrice());
            preparedStatement.setBigDecimal(8,
                    quotationDetail.getTaxRate() != null ? quotationDetail.getTaxRate() : BigDecimal.ZERO);
            preparedStatement.setBigDecimal(9,
                    quotationDetail.getDiscountRate() != null ? quotationDetail.getDiscountRate() : BigDecimal.ZERO);
            preparedStatement.setString(10, quotationDetail.getRejectedReason());

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
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
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, purchaseId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {

                QuotationDetail q = new QuotationDetail();
                q.setQuotationId(rs.getInt("quotation_id"));
                q.setId(rs.getInt("quotation_detail_id"));
                q.setPurchaseDetailId(rs.getInt("purchase_request_detail_id"));
                q.setAssetTypeId(rs.getInt("asset_type_id"));
                q.setQuantity(rs.getInt("quantity"));
                q.setQuotationDetailNote(rs.getString("quotation_detail_note"));
                q.setWarrantyMonths(rs.getInt("warranty_months"));
                q.setPrice(rs.getBigDecimal("price"));
                q.setTaxRate(rs.getBigDecimal("tax_rate"));
                q.setDiscountRate(rs.getBigDecimal("discount_rate"));
                q.setRejectedReason(rs.getString("rejected_reason"));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return quotationDetails;
    }

    @Override
    public void deleteByQuotationId(Integer quotationId) {
        String sql = "DELETE FROM quotation_detail WHERE quotation_id = ?";
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, quotationId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<QuotationDetail> findByQuotationId(Integer quotationId) {

        String sql = "select * from quotation_detail where  quotation_id = ?";

        List<QuotationDetail> quotationDetails = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quotationId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {

                QuotationDetail q = new QuotationDetail();
                q.setQuotationId(rs.getInt("quotation_id"));
                q.setId(rs.getInt("quotation_detail_id"));
                q.setPurchaseDetailId(rs.getInt("purchase_request_detail_id"));
                q.setAssetTypeId(rs.getInt("asset_type_id"));
                q.setQuantity(rs.getInt("quantity"));
                q.setQuotationDetailNote(rs.getString("quotation_detail_note"));
                q.setWarrantyMonths(rs.getInt("warranty_months"));
                q.setPrice(rs.getBigDecimal("price"));
                q.setTaxRate(rs.getBigDecimal("tax_rate"));
                q.setDiscountRate(rs.getBigDecimal("discount_rate"));
                q.setRejectedReason(rs.getString("rejected_reason"));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotationDetails;
    }

    @Override
    public List<edu.fpt.groupfive.dto.response.QuotationDetailResponse> findDetailByQuotationId(
            Integer quotationId) {
        String sql = "SELECT qd.*, at.asset_type_name, pd.spec_requirement " +
                "FROM quotation_detail qd " +
                "LEFT JOIN asset_type at ON qd.asset_type_id = at.asset_type_id " +
                "LEFT JOIN purchase_request_detail pd ON qd.purchase_request_detail_id = pd.purchase_request_detail_id "
                +
                "WHERE qd.quotation_id = ?";

        List<QuotationDetailResponse> responses = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quotationId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                responses.add(edu.fpt.groupfive.dto.response.QuotationDetailResponse.builder()
                        .quotationDetailId(rs.getInt("quotation_detail_id"))
                        .quotationId(rs.getInt("quotation_id"))
                        .purchaseDetailId(rs.getInt("purchase_request_detail_id"))
                        .assetTypeName(rs.getString("asset_type_name"))
                        .quantity(rs.getInt("quantity"))
                        .warrantyMonths(rs.getInt("warranty_months"))
                        .price(rs.getBigDecimal("price"))
                        .taxRate(rs.getBigDecimal("tax_rate"))
                        .discountRate(rs.getBigDecimal("discount_rate"))
                        .quotationDetailNote(rs.getString("quotation_detail_note"))
                        .specificationRequirement(rs.getString("spec_requirement"))
                        .rejectedReason(rs.getString("rejected_reason"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return responses;
    }
}
