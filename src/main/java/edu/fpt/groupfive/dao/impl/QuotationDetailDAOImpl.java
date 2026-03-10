package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
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

    // insert quotation detail
    @Override
    public Integer insert(QuotationDetail quotationDetail, Connection connection) {
        String sql = "insert into quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, " +
                "quantity," +
                "quotation_detail_note, warranty_months, price, tax_rate, discount_rate, reject_reason, spec_requirement) values (?,?,?,?,?,?,?,?,?,?,?)";

        try (
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
            preparedStatement.setString(11, quotationDetail.getSpecificationRequirement());

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

    // tìm kiếm theo purcahse id
    @Override
    public List<QuotationDetail> findByPurchaseId(Integer purchaseId) {

        String sql = "select qd.* from quotation q join quotation_detail qd on q.quotation_id = qd.quotation_id where" +
                " q.purchase_request_id = ? and qd.status <> 'CANCELLED' ";

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
                q.setRejectedReason(rs.getString("reject_reason"));
                q.setSpecificationRequirement(rs.getString("spec_requirement"));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return quotationDetails;
    }

    @Override
    public void deleteByQuotationId(Integer quotationId) {
        String sql = "update quotation_detail set status = 'CANCELLED'  where quotation_id = ?";

        Connection connection = null;
        try {
            connection = databaseConfig.getConnection();

            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, quotationId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {

                }
            }

            throw new RuntimeException(e);
        } finally {

            // reset auto commit lại về true và đóng cổng.
            if (connection != null)
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception ignored) {
                }
        }
    }

    // xóa quotation detail theo quotation id
    @Override
    public void deleteByQuotationId(Integer quotationId, Connection connection) {
        String sql = "delete from quotation_detail where quotation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quotationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // lấy ra quotation detail theo quotation id
    @Override
    public List<QuotationDetail> findByQuotationId(Integer quotationId) {

        String sql = "select qd.* from quotation_detail qd join dbo.quotation q on qd.quotation_id = q" +
                ".quotation_id where qd.quotation_id = ? and q.status != 'CANCELLED' ";

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
                q.setRejectedReason(rs.getString("reject_reason"));
                q.setSpecificationRequirement(rs.getString("spec_requirement"));

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lấy danh sách chi tiết báo giá thất bại", e);
        }
        return quotationDetails;
    }

}
