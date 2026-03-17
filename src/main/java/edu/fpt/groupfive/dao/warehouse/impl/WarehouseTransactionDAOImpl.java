package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseTransactionDAO;
import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WarehouseTransactionDAOImpl implements WarehouseTransactionDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<WarehouseTransaction> rowMapper = (rs, rowNum) -> new WarehouseTransaction(
            rs.getInt("transaction_id"),
            rs.getInt("asset_id"),
            rs.getInt("zone_id"),
            rs.getString("transaction_type"),
            rs.getInt("executed_by"),
            rs.getTimestamp("executed_at").toLocalDateTime(),
            rs.getString("note")
    );

    @Override
    public int insert(WarehouseTransaction transaction) {
        String sql = "INSERT INTO wh_transactions (asset_id, zone_id, transaction_type, executed_by, executed_at, note) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, transaction.getAssetId());
            ps.setInt(2, transaction.getZoneId());
            ps.setString(3, transaction.getTransactionType());
            ps.setInt(4, transaction.getExecutedBy());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getExecutedAt()));
            ps.setString(6, transaction.getNote());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    @Override
    public List<WarehouseTransaction> findRecent(int limit) {
        String sql = "SELECT TOP (?) * FROM wh_transactions ORDER BY executed_at DESC";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    @Override
    public List<WarehouseTransaction> findAll() {
        String sql = "SELECT * FROM wh_transactions ORDER BY executed_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void mapPoTransaction(Integer poId, Integer transactionId) {
        String sql = "INSERT INTO map_po_transactions (purchase_order_id, transaction_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, poId, transactionId);
    }

    @Override
    public void mapReturnTransaction(Integer returnId, Integer transactionId) {
        String sql = "INSERT INTO map_return_transactions (return_request_id, transaction_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, returnId, transactionId);
    }

    @Override
    public void mapAllocationTransaction(Integer allocationId, Integer transactionId) {
        String sql = "INSERT INTO map_allocation_transactions (allocation_request_id, transaction_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, allocationId, transactionId);
    }
}
