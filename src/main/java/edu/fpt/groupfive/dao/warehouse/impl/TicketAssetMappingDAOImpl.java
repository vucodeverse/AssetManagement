package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketAssetMappingDAO;
import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TicketAssetMappingDAOImpl implements TicketAssetMappingDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<TicketAssetMapping> rowMapper = new RowMapper<TicketAssetMapping>() {
        @Override
        public TicketAssetMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TicketAssetMapping.builder()
                    .id(rs.getInt("id"))
                    .detailId(rs.getInt("detail_id"))
                    .assetId(rs.getInt("asset_id"))
                    .build();
        }
    };

    @Override
    public int insert(TicketAssetMapping mapping) {
        String sql = "INSERT INTO wh_ticket_asset_mapping (detail_id, asset_id) VALUES (?, ?)";
        return jdbcTemplate.update(sql, mapping.getDetailId(), mapping.getAssetId());
    }

    @Override
    public List<TicketAssetMapping> findByDetailId(Integer detailId) {
        String sql = "SELECT * FROM wh_ticket_asset_mapping WHERE detail_id = ?";
        return jdbcTemplate.query(sql, rowMapper, detailId);
    }
}
