package edu.fpt.groupfive.dao.impl.warehouse;

import edu.fpt.groupfive.dao.warehouse.TicketAssetMappingDAO;
import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class TicketAssetMappingDAOImpl implements TicketAssetMappingDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insert(TicketAssetMapping mapping) {
        String sql = "INSERT INTO wh_ticket_asset_mapping (detail_id, asset_id, qc_report_id, updated_at) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                mapping.getDetailId(),
                mapping.getAssetId(),
                mapping.getQcReportId(),
                mapping.getUpdatedAt() != null ? Timestamp.valueOf(mapping.getUpdatedAt())
                        : new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public int countUnmatchedDetails(Integer ticketId) {
        // Query đếm số lượng các dòng TicketDetail chưa được map đủ số lượng quantity.
        String sql = "SELECT COUNT(*) FROM wh_ticket_detail td " +
                "LEFT JOIN (" +
                "    SELECT detail_id, COUNT(asset_id) as mapped_qty " +
                "    FROM wh_ticket_asset_mapping " +
                "    GROUP BY detail_id" +
                ") AS m ON td.id = m.detail_id " +
                "WHERE td.ticket_id = ? AND td.quantity > ISNULL(m.mapped_qty, 0)";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ticketId);
        return count != null ? count : 0;
    }
}
