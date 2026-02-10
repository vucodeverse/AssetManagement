package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

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
            preparedStatement.setInt(2, quotation.getSupplier().getId());
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
}
