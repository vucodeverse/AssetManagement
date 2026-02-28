package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.RackDAO;
import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.model.warehouse.Rack;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RackDAOImpl implements RackDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Rack create(Rack rack) {
        String sql = """
                INSERT INTO rack (warehouse_id, name, description, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        rack.setCreatedAt(now);
        rack.setUpdatedAt(now);
        rack.setStatus("ACTIVE");

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, rack.getWarehouseId());
            ps.setString(2, rack.getName());
            ps.setString(3, rack.getDescription());
            ps.setString(4, rack.getStatus());
            ps.setObject(5, rack.getCreatedAt());
            ps.setObject(6, rack.getUpdatedAt());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            rack.setId(keyHolder.getKey().intValue());
        }
        return rack;
    }

    @Override
    public Rack update(Rack rack) {
        String sql = """
                UPDATE rack SET name = ?, description = ?, updated_at = ?
                WHERE id = ?
                """;
        LocalDateTime now = LocalDateTime.now();
        rack.setUpdatedAt(now);
        int rows = jdbcTemplate.update(sql,
                rack.getName(),
                rack.getDescription(),
                rack.getUpdatedAt(),
                rack.getId());
        if (rows == 0) {
            throw new RuntimeException("Không thể cập nhật kệ id=" + rack.getId());
        }
        return rack;
    }

    @Override
    public Optional<Rack> getById(Integer id) {
        String sql = "SELECT id, warehouse_id, name, description, status, created_at, updated_at FROM rack WHERE id = ?";
        List<Rack> result = jdbcTemplate.query(sql, this::toModel, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<RackRespDto> getDetailById(Integer id) {
        String sql = """
                SELECT r.id, r.name, r.description, r.status, r.warehouse_id,
                       w.name AS warehouse_name,
                       (SELECT COUNT(1) FROM shelf s WHERE s.rack_id = r.id) AS shelf_count
                FROM rack r
                JOIN warehouse w ON r.warehouse_id = w.id
                WHERE r.id = ?
                """;
        List<RackRespDto> result = jdbcTemplate.query(sql, this::toDto, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<RackRespDto> getAllByWarehouseId(Integer warehouseId) {
        String sql = """
                SELECT r.id, r.name, r.description, r.status, r.warehouse_id,
                       w.name AS warehouse_name,
                       (SELECT COUNT(1) FROM shelf s WHERE s.rack_id = r.id) AS shelf_count
                FROM rack r
                JOIN warehouse w ON r.warehouse_id = w.id
                WHERE r.warehouse_id = ?
                ORDER BY r.created_at ASC
                """;
        return jdbcTemplate.query(sql, this::toDto, warehouseId);
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM rack WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existById(Integer id) {
        String sql = "SELECT COUNT(1) FROM rack WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Rack toModel(ResultSet rs, int rowNum) throws SQLException {
        Rack rack = new Rack();
        rack.setId(rs.getInt("id"));
        rack.setWarehouseId(rs.getInt("warehouse_id"));
        rack.setName(rs.getString("name"));
        rack.setDescription(rs.getString("description"));
        rack.setStatus(rs.getString("status"));
        return rack;
    }

    private RackRespDto toDto(ResultSet rs, int rowNum) throws SQLException {
        String status = rs.getString("status");
        return new RackRespDto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                status,
                "ACTIVE".equals(status),
                rs.getInt("warehouse_id"),
                rs.getString("warehouse_name"),
                rs.getInt("shelf_count")
        );
    }
}
