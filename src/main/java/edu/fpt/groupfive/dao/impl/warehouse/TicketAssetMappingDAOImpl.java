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

        @Override
        public java.util.List<edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto> getMappedAssetsByTicketId(
                        Integer ticketId) {
                String sql = "SELECT m.detail_id, m.asset_id, a.asset_name, at.name as asset_type_name " +
                                "FROM wh_ticket_asset_mapping m " +
                                "JOIN wh_ticket_detail td ON m.detail_id = td.id " +
                                "JOIN asset a ON m.asset_id = a.asset_id " +
                                "JOIN asset_type at ON a.asset_type_id = at.id " +
                                "WHERE td.ticket_id = ?";

                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                        edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto dto = new edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto();
                        dto.setDetailId(rs.getInt("detail_id"));
                        dto.setAssetId(rs.getInt("asset_id"));
                        dto.setAssetName(rs.getString("asset_name"));
                        dto.setAssetTypeName(rs.getString("asset_type_name"));
                        return dto;
                }, ticketId);
        }

        @Override
        public java.util.List<edu.fpt.groupfive.dto.warehouse.response.TicketDetailMappingDto> getDetailMappingsByTicketId(
                        Integer ticketId) {
                String sql = "SELECT td.id as detail_id, at.id as asset_type_id, at.name as asset_type_name, " +
                                "td.quantity as quantity_requested, ISNULL(m.mapped_qty, 0) as quantity_mapped, td.note "
                                +
                                "FROM wh_ticket_detail td " +
                                "JOIN asset_type at ON td.asset_type_id = at.id " +
                                "LEFT JOIN ( " +
                                "    SELECT detail_id, COUNT(asset_id) as mapped_qty " +
                                "    FROM wh_ticket_asset_mapping " +
                                "    GROUP BY detail_id" +
                                ") as m ON td.id = m.detail_id " +
                                "WHERE td.ticket_id = ?";

                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                        edu.fpt.groupfive.dto.warehouse.response.TicketDetailMappingDto dto = new edu.fpt.groupfive.dto.warehouse.response.TicketDetailMappingDto();
                        dto.setDetailId(rs.getInt("detail_id"));
                        dto.setAssetTypeId(rs.getInt("asset_type_id"));
                        dto.setAssetTypeName(rs.getString("asset_type_name"));
                        dto.setQuantityRequested(rs.getInt("quantity_requested"));
                        dto.setQuantityMapped(rs.getInt("quantity_mapped"));
                        dto.setNote(rs.getString("note"));
                        return dto;
                }, ticketId);
        }
}
