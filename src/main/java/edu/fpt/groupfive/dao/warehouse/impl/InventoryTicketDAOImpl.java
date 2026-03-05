package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InventoryTicketDAOImpl implements InventoryTicketDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<InventoryTicket> rowMapper = new RowMapper<InventoryTicket>() {
        @Override
        public InventoryTicket mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InventoryTicket.builder()
                    .id(rs.getInt("id"))
                    .warehouseId(rs.getInt("warehouse_id"))
                    .ticketType(rs.getString("ticket_type"))
                    .status(rs.getString("status"))
                    .createdBy(rs.getInt("created_by"))
                    .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                            : null)
                    .completedAt(
                            rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toLocalDateTime()
                                    : null)
                    .note(rs.getString("note"))
                    .build();
        }
    };

    @Override
    public int insert(InventoryTicket ticket) {
        String sql = "INSERT INTO wh_inventory_ticket (warehouse_id, ticket_type, status, created_by, note) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, ticket.getWarehouseId(), ticket.getTicketType(), ticket.getStatus(),
                ticket.getCreatedBy(), ticket.getNote());
    }

    @Override
    public int update(InventoryTicket ticket) {
        String sql = "UPDATE wh_inventory_ticket SET status = ?, completed_at = ?, note = ? WHERE id = ?";
        return jdbcTemplate.update(sql, ticket.getStatus(), ticket.getCompletedAt(), ticket.getNote(), ticket.getId());
    }

    @Override
    public InventoryTicket findById(Integer id) {
        String sql = "SELECT * FROM wh_inventory_ticket WHERE id = ?";
        List<InventoryTicket> result = jdbcTemplate.query(sql, rowMapper, id);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<InventoryTicket> findByWarehouseId(Integer warehouseId) {
        String sql = "SELECT * FROM wh_inventory_ticket WHERE warehouse_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }
}
