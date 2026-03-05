package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AuditScanRecordDAO;
import edu.fpt.groupfive.model.warehouse.AuditScanRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AuditScanRecordDAOImpl implements AuditScanRecordDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<AuditScanRecord> rowMapper = new RowMapper<AuditScanRecord>() {
        @Override
        public AuditScanRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AuditScanRecord.builder()
                    .id(rs.getInt("id"))
                    .auditId(rs.getInt("audit_id"))
                    .assetId(rs.getInt("asset_id"))
                    .matchStatus(rs.getString("match_status"))
                    .scannedAt(rs.getTimestamp("scanned_at") != null ? rs.getTimestamp("scanned_at").toLocalDateTime()
                            : null)
                    .actionTaken(rs.getString("action_taken"))
                    .build();
        }
    };

    @Override
    public int insert(AuditScanRecord record) {
        String sql = "INSERT INTO wh_audit_scan_record (audit_id, asset_id, match_status, action_taken) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, record.getAuditId(), record.getAssetId(), record.getMatchStatus(),
                record.getActionTaken());
    }

    @Override
    public List<AuditScanRecord> findByAuditId(Integer auditId) {
        String sql = "SELECT * FROM wh_audit_scan_record WHERE audit_id = ? ORDER BY scanned_at DESC";
        return jdbcTemplate.query(sql, rowMapper, auditId);
    }
}
