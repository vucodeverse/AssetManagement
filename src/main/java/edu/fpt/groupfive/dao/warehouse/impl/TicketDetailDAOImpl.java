package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TicketDetailDAOImpl implements TicketDetailDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TicketDetail> rowMapper = (rs, rowNum) -> {
        TicketDetail detail = new TicketDetail();
        detail.setId(rs.getInt("id"));
        detail.setTicketId(rs.getInt("ticket_id"));
        detail.setAssetTypeId(rs.getInt("asset_type_id"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setNote(rs.getString("note"));
        return detail;
    };

    @Override
    public List<TicketDetail> findByTicketId(Integer ticketId) {
        String sql = "SELECT id, ticket_id, asset_type_id, quantity, note FROM wh_ticket_detail WHERE ticket_id = ?";
        return jdbcTemplate.query(sql, rowMapper, ticketId);
    }

    @Override
    public void insert(TicketDetail detail) {
        String sql = "INSERT INTO wh_ticket_detail (ticket_id, asset_type_id, quantity, note) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, detail.getTicketId());
            ps.setInt(2, detail.getAssetTypeId());
            ps.setInt(3, detail.getQuantity());
            ps.setString(4, detail.getNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            detail.setId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public void insertBatch(List<TicketDetail> details) {
        String sql = "INSERT INTO wh_ticket_detail (ticket_id, asset_type_id, quantity, note) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, details, details.size(), (ps, detail) -> {
            ps.setInt(1, detail.getTicketId());
            ps.setInt(2, detail.getAssetTypeId());
            ps.setInt(3, detail.getQuantity());
            ps.setString(4, detail.getNote());
        });
    }

    @Override
    public void update(TicketDetail detail) {
        String sql = "UPDATE wh_ticket_detail SET asset_type_id = ?, quantity = ?, note = ? WHERE id = ?";
        jdbcTemplate.update(sql, detail.getAssetTypeId(), detail.getQuantity(), detail.getNote(), detail.getId());
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM wh_ticket_detail WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByTicketId(Integer ticketId) {
        String sql = "DELETE FROM wh_ticket_detail WHERE ticket_id = ?";
        jdbcTemplate.update(sql, ticketId);
    }
}
