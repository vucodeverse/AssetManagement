package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.ShelfDAO;
import edu.fpt.groupfive.dto.warehouse.ShelfRespDto;
import edu.fpt.groupfive.model.warehouse.Shelf;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShelfDAOImpl implements ShelfDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Shelf create(Shelf shelf) {
        String sql = """
                INSERT INTO shelf (name, current_capacity, max_capacity, description, rack_id, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDate now = LocalDate.now();
        shelf.setCreatedAt(now);
        shelf.setUpdatedAt(now);
        shelf.setStatus("ACTIVE");
        if (shelf.getCurrentCapacity() == null) {
            shelf.setCurrentCapacity(0);
        }

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, shelf.getName());
            ps.setInt(2, shelf.getCurrentCapacity());
            ps.setInt(3, shelf.getMaxCapacity());
            ps.setString(4, shelf.getDescription());
            ps.setInt(5, shelf.getRackId());
            ps.setString(6, shelf.getStatus());
            ps.setObject(7, shelf.getCreatedAt());
            ps.setObject(8, shelf.getUpdatedAt());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            shelf.setId(keyHolder.getKey().intValue());
        }
        return shelf;
    }

    @Override
    public Shelf update(Shelf shelf) {
        String sql = """
                UPDATE shelf SET name = ?, max_capacity = ?, description = ?, updated_at = ?
                WHERE id = ?
                """;
        LocalDate now = LocalDate.now();
        shelf.setUpdatedAt(now);
        int rows = jdbcTemplate.update(sql,
                shelf.getName(),
                shelf.getMaxCapacity(),
                shelf.getDescription(),
                shelf.getUpdatedAt(),
                shelf.getId());
        if (rows == 0) {
            throw new RuntimeException("Không thể cập nhật tầng id=" + shelf.getId());
        }
        return shelf;
    }

    @Override
    public Optional<Shelf> getById(Integer id) {
        String sql = "SELECT id, name, current_capacity, max_capacity, description, rack_id, status, created_at, updated_at FROM shelf WHERE id = ?";
        List<Shelf> result = jdbcTemplate.query(sql, this::toModel, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<ShelfRespDto> getDetailById(Integer id) {
        String sql = """
                SELECT s.id, s.name, s.current_capacity, s.max_capacity, s.description, s.status,
                       s.rack_id, r.name AS rack_name, r.warehouse_id
                FROM shelf s
                JOIN rack r ON s.rack_id = r.id
                WHERE s.id = ?
                """;
        List<ShelfRespDto> result = jdbcTemplate.query(sql, this::toDto, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<ShelfRespDto> getAllByRackId(Integer rackId) {
        String sql = """
                SELECT s.id, s.name, s.current_capacity, s.max_capacity, s.description, s.status,
                       s.rack_id, r.name AS rack_name, r.warehouse_id
                FROM shelf s
                JOIN rack r ON s.rack_id = r.id
                WHERE s.rack_id = ?
                ORDER BY s.name ASC
                """;
        return jdbcTemplate.query(sql, this::toDto, rackId);
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM shelf WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existById(Integer id) {
        String sql = "SELECT COUNT(1) FROM shelf WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Shelf toModel(ResultSet rs, int rowNum) throws SQLException {
        Shelf shelf = new Shelf();
        shelf.setId(rs.getInt("id"));
        shelf.setName(rs.getString("name"));
        shelf.setCurrentCapacity(rs.getInt("current_capacity"));
        shelf.setMaxCapacity(rs.getInt("max_capacity"));
        shelf.setDescription(rs.getString("description"));
        shelf.setRackId(rs.getInt("rack_id"));
        shelf.setStatus(rs.getString("status"));
        return shelf;
    }

    private ShelfRespDto toDto(ResultSet rs, int rowNum) throws SQLException {
        String status = rs.getString("status");
        return new ShelfRespDto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("current_capacity"),
                rs.getInt("max_capacity"),
                rs.getString("description"),
                status,
                "ACTIVE".equals(status),
                rs.getInt("rack_id"),
                rs.getString("rack_name"),
                rs.getInt("warehouse_id")
        );
    }
}
