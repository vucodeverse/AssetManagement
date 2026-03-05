package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketQcHistoryDAO;
import edu.fpt.groupfive.model.warehouse.TicketQcHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TicketQcHistoryDAOImpl implements TicketQcHistoryDAO {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<TicketQcHistory> rowMapper = new RowMapper<TicketQcHistory>() {
        @Override
        public TicketQcHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TicketQcHistory.builder()
                    .id(rs.getInt("id"))
                    .ticketId(rs.getInt("ticket_id"))
                    .assetId(rs.getInt("asset_id"))
                    .qcStatus(rs.getString("qc_status"))
                    .inspectedBy(rs.getInt("inspected_by"))
                    .qcDate(rs.getTimestamp("qc_date") != null ? rs.getTimestamp("qc_date").toLocalDateTime() : null)
                    .note(rs.getString("note"))
                    .build();
        }
    };

    @Override
    public int insert(TicketQcHistory history) {
        String sql = "INSERT INTO wh_ticket_qc_history (ticket_id, asset_id, qc_status, inspected_by, note) VALUES (?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, history.getTicketId());
            ps.setInt(2, history.getAssetId());
            ps.setString(3, history.getQcStatus());
            ps.setInt(4, history.getInspectedBy());
            ps.setString(5, history.getNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            history.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public List<TicketQcHistory> findByTicketId(Integer ticketId) {
        String sql = "SELECT * FROM wh_ticket_qc_history WHERE ticket_id = ? ORDER BY qc_date DESC";
        return jdbcTemplate.query(sql, rowMapper, ticketId);
    }

    @Override
    public List<TicketQcHistory> findByAssetId(Integer assetId) {
        String sql = "SELECT * FROM wh_ticket_qc_history WHERE asset_id = ? ORDER BY qc_date DESC";
        return jdbcTemplate.query(sql, rowMapper, assetId);
    }
}
