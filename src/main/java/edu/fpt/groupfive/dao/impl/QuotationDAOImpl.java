package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuotationDAOImpl implements QuotationDAO {
    private final DatabaseConfig databaseConfig;

    @Override
    public Integer insert(Quotation quotation) {

        String sql = "insert into quotation (purchase_request_id, supplier_id, status, total_amount, " +
                "created_at) VALUES (?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setInt(1, quotation.getPurchaseId());
            preparedStatement.setInt(2, quotation.getSupplierId());
            preparedStatement.setString(3, quotation.getStatus().toString());
            preparedStatement.setBigDecimal(4, quotation.getTotalAmount());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(quotation.getCreatedAt().atStartOfDay()));

            preparedStatement.executeUpdate();
            ResultSet rs  = preparedStatement.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public Optional<Quotation> findById(Integer quotationId) {

        String sql = "select * from quotation where quotation_id = ?";


        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, quotationId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){

                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setStatus(QuotationStatus.valueOf(rs.getString("status")));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation.setCreatedAt(rs.getDate("created_at") != null ?
                        rs.getDate("created_at").toLocalDate() : null );
                quotation.setUpdatedAt(rs.getDate("updated_at") != null ?
                        rs.getDate("updated_at").toLocalDate() : null);

                return Optional.of(quotation);
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<Quotation> findByPurchaseId(Integer purchaseId) {

        String sql = "select * from quotation where purchase_request_id = ?";

        List<Quotation> quotations = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){

                Quotation quotation = new Quotation();
                quotation.setId(rs.getInt("quotation_id"));
                quotation.setPurchaseId(rs.getInt("purchase_request_id"));
                quotation.setSupplierId(rs.getInt("supplier_id"));
                quotation.setStatus(QuotationStatus.valueOf(rs.getString("status")));
                quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
                quotation.setCreatedAt(rs.getDate("created_at") != null ?
                        rs.getDate("created_at").toLocalDate() : null );
                quotation.setUpdatedAt(rs.getDate("updated_at") != null ?
                        rs.getDate("updated_at").toLocalDate() : null);
                quotations.add(quotation);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return quotations;
    }

    @Override
    public List<Quotation> getAll() {
        return List.of();
    }

    @Override
    public Integer countQuotationFromPurchaseId(Integer purchaseId) {

        String sql = "select p.purchase_request_id, count(distinct q.quotation_id) from purchase_request p left join quotation q on p\n" +
                "    .purchase_request_id = q\n" +
                "    .purchase_request_id where p\n" +
                "    .purchase_request_id = ? group by p.purchase_request_id";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                return rs.getInt(2);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public BigDecimal totalAmoutForPurchaseId(Integer purchaseId) {

        String sql = "select min(q.total_amount) from quotation q where q.purchase_request_id = ?";


        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, purchaseId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){

                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  BigDecimal.ZERO;
    }
}
