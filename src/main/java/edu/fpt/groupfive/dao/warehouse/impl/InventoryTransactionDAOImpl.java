package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTransactionDAO;
import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InventoryTransactionDAOImpl implements InventoryTransactionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<InventoryTransaction> rowMapper = new RowMapper<InventoryTransaction>() {
        @Override
        public InventoryTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InventoryTransaction.builder()
                    .id(rs.getInt("id"))
                    .assetId(rs.getInt("asset_id"))
                    .ticketId(rs.getObject("ticket_id") != null ? rs.getInt("ticket_id") : null)
                    .transactionType(rs.getString("transaction_type"))
                    .fromZoneId(rs.getObject("from_zone_id") != null ? rs.getInt("from_zone_id") : null)
                    .toZoneId(rs.getObject("to_zone_id") != null ? rs.getInt("to_zone_id") : null)
                    .performerId(rs.getInt("performer_id"))
                    .transactionDate(rs.getTimestamp("transaction_date") != null
                            ? rs.getTimestamp("transaction_date").toLocalDateTime()
                            : null)
                    .build();
        }
    };

    @Override
    public int insert(InventoryTransaction transaction) {
        String sql = "INSERT INTO wh_inventory_transaction (asset_id, ticket_id, transaction_type, from_zone_id, to_zone_id, performer_id) VALUES (?, ?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, transaction.getAssetId());
            if (transaction.getTicketId() != null) {
                ps.setInt(2, transaction.getTicketId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, transaction.getTransactionType());
            if (transaction.getFromZoneId() != null) {
                ps.setInt(4, transaction.getFromZoneId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            if (transaction.getToZoneId() != null) {
                ps.setInt(5, transaction.getToZoneId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setInt(6, transaction.getPerformerId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            transaction.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public List<InventoryTransaction> findByAssetId(Integer assetId) {
        String sql = "SELECT * FROM wh_inventory_transaction WHERE asset_id = ? ORDER BY transaction_date DESC";
        return jdbcTemplate.query(sql, rowMapper, assetId);
    }

    @Override
    public List<InventoryTransaction> findByTicketId(Integer ticketId) {
        String sql = "SELECT * FROM wh_inventory_transaction WHERE ticket_id = ? ORDER BY transaction_date DESC";
        return jdbcTemplate.query(sql, rowMapper, ticketId);
    }
}
