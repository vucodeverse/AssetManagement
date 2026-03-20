package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class WhTransactionDAOImpl implements WhTransactionDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Integer insert(WarehouseTransaction transaction) {
        try (Connection conn = databaseConfig.getConnection()) {
            return insert(transaction, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert warehouse transaction", e);
        }
    }

    @Override
    public Integer insert(WarehouseTransaction transaction, Connection conn) {
        String sql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getAssetId());
            ps.setInt(2, transaction.getZoneId());
            ps.setString(3, transaction.getTransactionType());
            ps.setInt(4, transaction.getExecutedBy());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getExecutedAt() != null ? transaction.getExecutedAt() : LocalDateTime.now()));
            ps.setString(6, transaction.getNote());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Creating warehouse transaction failed, no ID obtained.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert warehouse transaction", e);
        }
    }

    @Override
    public void linkPOToTransaction(Integer poId, Integer transactionId) {
        try (Connection conn = databaseConfig.getConnection()) {
            linkPOToTransaction(poId, transactionId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link PO to transaction", e);
        }
    }

    @Override
    public void linkPOToTransaction(Integer poId, Integer transactionId, Connection conn) {
        String sql = "INSERT INTO map_po_transactions (purchase_order_id, transaction_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            ps.setInt(2, transactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link PO to transaction", e);
        }
    }
}
