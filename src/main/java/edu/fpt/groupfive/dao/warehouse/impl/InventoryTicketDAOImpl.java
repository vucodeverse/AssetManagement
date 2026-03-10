package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryTicketDAOImpl implements InventoryTicketDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<InventoryTicket> rowMapper = (rs, rowNum) -> {
        InventoryTicket ticket = new InventoryTicket();
        ticket.setId(rs.getInt("id"));
        ticket.setWarehouseId(rs.getInt("warehouse_id"));
        ticket.setTicketType(TicketType.valueOf(rs.getString("ticket_type")));
        ticket.setStatus(HandleStatus.valueOf(rs.getString("status")));

        int handleBy = rs.getInt("handle_by");
        ticket.setHandleBy(rs.wasNull() ? null : handleBy);

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        ticket.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        java.sql.Timestamp completedAt = rs.getTimestamp("completed_at");
        ticket.setCompletedAt(completedAt != null ? completedAt.toLocalDateTime() : null);

        return ticket;
    };

    @Override
    public List<InventoryTicket> findAllByWarehouseId(Integer warehouseId) {
        String sql = "SELECT id, warehouse_id, ticket_type, status, handle_by, created_at, completed_at " +
                "FROM wh_inventory_ticket WHERE warehouse_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }

    @Override
    public List<InventoryTicket> findByWarehouseIdAndStatusIn(Integer warehouseId, List<HandleStatus> statuses) {
        if (statuses == null || statuses.isEmpty())
            return List.of();

        String inSql = statuses.stream().map(s -> "?").collect(Collectors.joining(", "));
        String sql = String.format(
                "SELECT id, warehouse_id, ticket_type, status, handle_by, created_at, completed_at " +
                        "FROM wh_inventory_ticket WHERE warehouse_id = ? AND status IN (%s) ORDER BY created_at DESC",
                inSql);

        Object[] params = new Object[statuses.size() + 1];
        params[0] = warehouseId;
        for (int i = 0; i < statuses.size(); i++) {
            params[i + 1] = statuses.get(i).name();
        }

        return jdbcTemplate.query(sql, rowMapper, params);
    }

    @Override
    public InventoryTicket findById(Integer id) {
        String sql = "SELECT id, warehouse_id, ticket_type, status, handle_by, created_at, completed_at " +
                "FROM wh_inventory_ticket WHERE id = ?";
        List<InventoryTicket> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public InventoryTicket insert(InventoryTicket ticket) {
        String sql = "INSERT INTO wh_inventory_ticket (warehouse_id, ticket_type, status, handle_by, created_at, completed_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ticket.getWarehouseId());
            ps.setString(2, ticket.getTicketType().name());
            ps.setString(3, ticket.getStatus().name());

            if (ticket.getHandleBy() != null) {
                ps.setInt(4, ticket.getHandleBy());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            if (ticket.getCreatedAt() != null) {
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(ticket.getCreatedAt()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }

            if (ticket.getCompletedAt() != null) {
                ps.setTimestamp(6, java.sql.Timestamp.valueOf(ticket.getCompletedAt()));
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }

            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            ticket.setId(keyHolder.getKey().intValue());
        }
        return ticket;
    }

    @Override
    public void update(InventoryTicket ticket) {
        String sql = "UPDATE wh_inventory_ticket SET ticket_type = ?, status = ?, handle_by = ?, completed_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                ticket.getTicketType().name(),
                ticket.getStatus().name(),
                ticket.getHandleBy(),
                ticket.getCompletedAt(),
                ticket.getId());
    }

    @Override
    public void updateStatus(Integer ticketId, HandleStatus status) {
        String sql = "UPDATE wh_inventory_ticket SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), ticketId);
    }
}
