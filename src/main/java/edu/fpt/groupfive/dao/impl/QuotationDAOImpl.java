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

        String sql = "insert into quotation (purchase_request_id, supplier_id, quotation_date, status, total_amount, " +
                "created_at) VALUES (?,?,?,?,?,?,?)";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setInt(1, quotation.getPurchase().getId());
            preparedStatement.setInt(2, quotation.getSupplier().getId());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(quotation.getQuoationDate().atStartOfDay()));
            preparedStatement.setString(4, quotation.getStatus().toString());
            preparedStatement.setBigDecimal(5, quotation.getTotalAmount());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(quotation.getCreatedAt().atStartOfDay()));

            preparedStatement.executeUpdate();
            ResultSet rs  = preparedStatement.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
