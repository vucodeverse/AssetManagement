package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.InventoryAuditDAO;
import edu.fpt.groupfive.model.warehouse.InventoryAudit;
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
public class InventoryAuditDAOImpl implements InventoryAuditDAO {

    private final JdbcTemplate jdbcTemplate;

    private RowMapper<InventoryAudit> rowMapper = new RowMapper<InventoryAudit>() {
        @Override
        public InventoryAudit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InventoryAudit.builder()
                    .id(rs.getInt("id"))
                    .warehouseId(rs.getInt("warehouse_id"))
                    .zoneId(rs.getInt("zone_id"))
                    .status(rs.getString("status"))
                    .auditorId(rs.getInt("auditor_id"))
                    .startTime(rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime()
                            : null)
                    .endTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null)
                    .note(rs.getString("note"))
                    .build();
        }
    };

    @Override
    public int insert(InventoryAudit audit) {
        String sql = "INSERT INTO wh_inventory_audit (warehouse_id, zone_id, status, auditor_id, note) VALUES (?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, audit.getWarehouseId());
            if (audit.getZoneId() != null) {
                ps.setInt(2, audit.getZoneId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, audit.getStatus());
            ps.setInt(4, audit.getAuditorId());
            ps.setString(5, audit.getNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            audit.setId(keyHolder.getKey().intValue());
            return keyHolder.getKey().intValue();
        }
        return 0;
    }

    @Override
    public int update(InventoryAudit audit) {
        String sql = "UPDATE wh_inventory_audit SET status = ?, end_time = ?, note = ? WHERE id = ?";
        return jdbcTemplate.update(sql, audit.getStatus(), audit.getEndTime(), audit.getNote(), audit.getId());
    }

    @Override
    public InventoryAudit findById(Integer id) {
        String sql = "SELECT * FROM wh_inventory_audit WHERE id = ?";
        List<InventoryAudit> result = jdbcTemplate.query(sql, rowMapper, id);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<InventoryAudit> findByWarehouseId(Integer warehouseId) {
        String sql = "SELECT * FROM wh_inventory_audit WHERE warehouse_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }
}
