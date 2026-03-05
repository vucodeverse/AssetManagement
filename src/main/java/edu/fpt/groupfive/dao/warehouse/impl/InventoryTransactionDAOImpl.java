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
        return jdbcTemplate.update(sql, transaction.getAssetId(), transaction.getTicketId(),
                transaction.getTransactionType(), transaction.getFromZoneId(), transaction.getToZoneId(),
                transaction.getPerformerId());
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
