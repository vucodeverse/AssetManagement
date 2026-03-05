package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TicketDetailDAOImpl implements TicketDetailDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<TicketDetail> rowMapper = new RowMapper<TicketDetail>() {
        @Override
        public TicketDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TicketDetail.builder()
                    .id(rs.getInt("id"))
                    .ticketId(rs.getInt("ticket_id"))
                    .assetTypeId(rs.getInt("asset_type_id"))
                    .expectedQuantity(rs.getInt("expected_quantity"))
                    .actualQuantity(rs.getInt("actual_quantity"))
                    .note(rs.getString("note"))
                    .build();
        }
    };

    @Override
    public int insert(TicketDetail detail) {
        String sql = "INSERT INTO wh_ticket_detail (ticket_id, asset_type_id, expected_quantity, actual_quantity, note) VALUES (?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, detail.getTicketId());
            ps.setInt(2, detail.getAssetTypeId());
            ps.setInt(3, detail.getExpectedQuantity());
            ps.setInt(4, detail.getActualQuantity());
            ps.setString(5, detail.getNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            detail.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public int update(TicketDetail detail) {
        String sql = "UPDATE wh_ticket_detail SET actual_quantity = ?, note = ? WHERE id = ?";
        return jdbcTemplate.update(sql, detail.getActualQuantity(), detail.getNote(), detail.getId());
    }

    @Override
    public List<TicketDetail> findByTicketId(Integer ticketId) {
        String sql = "SELECT * FROM wh_ticket_detail WHERE ticket_id = ?";
        return jdbcTemplate.query(sql, rowMapper, ticketId);
    }
}
