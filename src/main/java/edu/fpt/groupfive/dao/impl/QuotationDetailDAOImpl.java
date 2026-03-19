package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.util.exception.DataAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${dao.common.insert_error}")
    private String insertErrorMsg;

    @Value("${dao.quotation.detail.find_error}")
    private String findErrorMsg;

    @Value("${dao.quotation.detail.update_status_error}")
    private String updateStatusErrorMsg;

    @Value("${dao.quotation.detail.list_error}")
    private String listErrorMsg;

    // insert quotation detail
    @Override
    public Integer insert(QuotationDetail quotationDetail, Connection connection) {
        String sql = "insert into quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, " +
                "quantity," +
                "quotation_detail_note, warranty_months, price, tax_rate, discount_rate, reject_reason, spec_requirement, status) values (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, quotationDetail.getQuotationId());
            preparedStatement.setInt(2, quotationDetail.getPurchaseDetailId());
            preparedStatement.setInt(3, quotationDetail.getAssetTypeId());

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
            preparedStatement.setString(12,
                    quotationDetail.getQuotationDetailStatus() != null
                            ? quotationDetail.getQuotationDetailStatus().name()
                            : QuotationStatus.PENDING.name());

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
        String sql = "select qd.* from quotation_detail qd where qd.quotation_detail_id = ? and (qd.status is null or qd.status <> 'DELETED')";

        try (Connection connection = databaseConfig.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quotationDetailId);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {

                QuotationDetail q = new QuotationDetail();
                q.setId(rs.getInt("quotation_detail_id"));
                q.setQuotationId(rs.getInt("quotation_id"));
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

                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    q.setQuotationDetailStatus(QuotationStatus.valueOf(statusStr));
                }

                return Optional.of(q);
            }
        } catch (SQLException e) {
            throw new DataAccessException(findErrorMsg, e);
        }

        return Optional.empty();
    }

    // xóa quotation detail theo quotation id
    @Override
    public void deleteByQuotationId(Integer quotationId, Connection connection) {
        String sql = "update quotation_detail set status = 'DELETED' where quotation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quotationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Integer quotationDetailId, QuotationStatus quotationStatus) {
        String sql = "update quotation_detail set status = ? where quotation_detail_id = ?";

        try (Connection connection = databaseConfig.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, quotationStatus.name());
                ps.setInt(2, quotationDetailId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(updateStatusErrorMsg, e);
        }
    }

    // lấy ra quotation detail theo quotation id
    @Override
    public List<QuotationDetail> findByQuotationId(Integer quotationId) {

        String sql = "select qd.* from quotation_detail qd join dbo.quotation q on qd.quotation_id = q" +
                ".quotation_id where qd.quotation_id = ? and q.status <> 'DELETED'";

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

                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    q.setQuotationDetailStatus(QuotationStatus.valueOf(statusStr));
                }

                quotationDetails.add(q);
            }
        } catch (SQLException e) {
            throw new DataAccessException(listErrorMsg, e);
        }
        return quotationDetails;
    }

}
