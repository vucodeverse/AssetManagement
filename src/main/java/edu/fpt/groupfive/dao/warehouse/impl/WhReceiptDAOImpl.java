package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhReceiptDAO;
import edu.fpt.groupfive.model.warehouse.WhReceipt;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.Collections; // Not needed but safe

@Repository
@RequiredArgsConstructor
public class WhReceiptDAOImpl implements WhReceiptDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public int createReceipt(WhReceipt receipt) {
        String sql = "INSERT INTO wh_receipts (receipt_no, purchase_order_id, asset_handover_id, receipt_type, created_at, created_by, note) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, receipt.getReceiptNo());
            ps.setObject(2, receipt.getPurchaseOrderId());
            ps.setObject(3, receipt.getAssetHandoverId());
            ps.setString(4, receipt.getReceiptType());
            ps.setTimestamp(5,
                    Timestamp.valueOf(receipt.getCreatedAt() != null ? receipt.getCreatedAt() : LocalDateTime.now()));
            ps.setObject(6, receipt.getCreatedBy());
            ps.setString(7, receipt.getNote());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating receipt", e);
        }
        return -1;
    }

    @Override
    public Optional<WhReceipt> findById(int receiptId) {
        String sql = "SELECT * FROM wh_receipts WHERE receipt_id = ?";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WhReceipt receipt = WhReceipt.builder()
                            .receiptId(rs.getInt("receipt_id"))
                            .receiptNo(rs.getString("receipt_no"))
                            .purchaseOrderId((Integer) rs.getObject("purchase_order_id"))
                            .assetHandoverId((Integer) rs.getObject("asset_handover_id"))
                            .receiptType(rs.getString("receipt_type"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .createdBy((Integer) rs.getObject("created_by"))
                            .note(rs.getString("note"))
                            .build();
                    return Optional.of(receipt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding receipt", e);
        }
        return Optional.empty();
    }

    @Override
    public String generateNextReceiptNo(String type) {
        // Simple implementation for now: RCP-TYPE-TIMESTAMP
        return "PN-" + type + "-" + System.currentTimeMillis();
    }

    @Override
    public List<WhReceipt> findByPurchaseOrderId(Integer poId) {
        List<WhReceipt> list = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name + ' ' + u.last_name as creator_name, " +
                "(SELECT COUNT(*) FROM wh_transactions t WHERE t.receipt_id = r.receipt_id) as total_quantity " +
                "FROM wh_receipts r " +
                "JOIN users u ON r.created_by = u.user_id " +
                "WHERE r.purchase_order_id = ? " +
                "ORDER BY r.created_at DESC";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(WhReceipt.builder()
                            .receiptId(rs.getInt("receipt_id"))
                            .receiptNo(rs.getString("receipt_no"))
                            .purchaseOrderId((Integer) rs.getObject("purchase_order_id"))
                            .receiptType(rs.getString("receipt_type"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .createdBy((Integer) rs.getObject("created_by"))
                            .creatorName(rs.getString("creator_name"))
                            .totalQuantity(rs.getInt("total_quantity"))
                            .note(rs.getString("note"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding WhReceipt by PO ID", e);
        }
        return list;
    }

    @Override
    public List<WhReceipt> findByAssetHandoverId(Integer handoverId) {
        List<WhReceipt> list = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name + ' ' + u.last_name as creator_name, " +
                "(SELECT COUNT(*) FROM wh_transactions t WHERE t.receipt_id = r.receipt_id) as total_quantity " +
                "FROM wh_receipts r " +
                "LEFT JOIN users u ON r.created_by = u.user_id " +
                "WHERE r.asset_handover_id = ? " +
                "ORDER BY r.created_at DESC";
        try (Connection conn = databaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, handoverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(WhReceipt.builder()
                            .receiptId(rs.getInt("receipt_id"))
                            .receiptNo(rs.getString("receipt_no"))
                            .assetHandoverId((Integer) rs.getObject("asset_handover_id"))
                            .receiptType(rs.getString("receipt_type"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .createdBy((Integer) rs.getObject("created_by"))
                            .creatorName(rs.getString("creator_name"))
                            .totalQuantity(rs.getInt("total_quantity"))
                            .note(rs.getString("note"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding WhReceipt by Handover ID", e);
        }
        return list;
    }
}
