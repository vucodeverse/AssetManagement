package edu.fpt.groupfive.dao.impl.warehouse;

import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.util.exception.ZoneNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ZoneDAOImpl implements ZoneDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Zone> rowMapper = (rs, rowNum) -> {
        Zone z = new Zone();
        z.setId(rs.getInt("id"));
        z.setWarehouseId(rs.getInt("warehouse_id"));
        z.setName(rs.getString("name"));
        // assigned_asset_type_id có thể null
        int typeId = rs.getInt("assigned_asset_type_id");
        z.setAssignedAssetTypeId(rs.wasNull() ? null : typeId);
        z.setMaxCapacity(rs.getInt("max_capacity"));
        z.setCurrentCapacity(rs.getInt("current_capacity"));
        z.setStatus(ActiveStatus.valueOf(rs.getString("status")));
        return z;
    };

    @Override
    public List<Zone> findAll() {
        String sql = "SELECT id, warehouse_id, name, assigned_asset_type_id, max_capacity, current_capacity, status FROM wh_zones";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Zone> findByWarehouseId(Integer warehouseId) {
        String sql = "SELECT id, warehouse_id, name, assigned_asset_type_id, max_capacity, current_capacity, status " +
                "FROM wh_zones WHERE warehouse_id = ?";
        return jdbcTemplate.query(sql, rowMapper, warehouseId);
    }

    @Override
    public Zone findById(Integer id) {
        String sql = "SELECT id, warehouse_id, name, assigned_asset_type_id, max_capacity, current_capacity, status " +
                "FROM wh_zones WHERE id = ?";
        List<Zone> list = jdbcTemplate.query(sql, rowMapper, id);
        if (list.isEmpty()) {
            throw new ZoneNotFoundException(id);
        }
        return list.get(0);
    }

    @Override
    public Zone create(Zone zone) {
        String sql = "INSERT INTO wh_zones (warehouse_id, name, assigned_asset_type_id, max_capacity, current_capacity, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, zone.getWarehouseId());
            ps.setString(2, zone.getName());
            if (zone.getAssignedAssetTypeId() != null) {
                ps.setInt(3, zone.getAssignedAssetTypeId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, zone.getMaxCapacity());
            ps.setInt(5, zone.getCurrentCapacity() != null ? zone.getCurrentCapacity() : 0);
            ps.setString(6, zone.getStatus() != null ? zone.getStatus().name() : ActiveStatus.ACTIVE.name());
            return ps;
        }, keyHolder);
        zone.setId(keyHolder.getKey().intValue());
        return zone;
    }

    @Override
    public Zone update(Zone zone) {
        String sql = "UPDATE wh_zones SET name = ?, assigned_asset_type_id = ?, max_capacity = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                zone.getName(),
                zone.getAssignedAssetTypeId(),
                zone.getMaxCapacity(),
                zone.getId());
        return zone;
    }

    @Override
    public void updateStatus(Integer id, ActiveStatus status) {
        String sql = "UPDATE wh_zones SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }

    @Override
    public void batchDecreaseCapacityByTicketId(Integer ticketId) {
        // Find how many assets are removed from each zone
        String sql = "UPDATE wh_zones " +
                "SET current_capacity = current_capacity - m.qty_to_remove " +
                "FROM wh_zones z " +
                "JOIN ( " +
                "    SELECT al.zone_id, COUNT(tam.asset_id) as qty_to_remove " +
                "    FROM wh_ticket_asset_mapping tam " +
                "    JOIN wh_ticket_detail td ON tam.detail_id = td.id " +
                "    JOIN wh_asset_location al ON tam.asset_id = al.asset_id " +
                "    WHERE td.ticket_id = ? " +
                "    GROUP BY al.zone_id " +
                ") m ON z.id = m.zone_id";
        jdbcTemplate.update(sql, ticketId);
    }

    @Override
    public void batchIncreaseCapacity(Map<Integer, Integer> zoneCapacityIncrements) {
        String sql = "UPDATE wh_zones SET current_capacity = current_capacity + ? WHERE id = ?";
        List<Object[]> batchArgs = zoneCapacityIncrements.entrySet().stream()
                .map(entry -> new Object[] { entry.getValue(), entry.getKey() })
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
