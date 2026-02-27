package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.model.warehouse.WarehouseStatus;
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
public class WarehouseDAOImpl implements WarehouseDAO {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Warehouse create(Warehouse newWarehouse) {

        String sql = """
                INSERT INTO warehouse (name, address, status, manager_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        newWarehouse.setCreatedAt(now);
        newWarehouse.setUpdatedAt(now);
        newWarehouse.deactive();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, newWarehouse.getName());
                    ps.setString(2, newWarehouse.getAddress());
                    ps.setString(3, newWarehouse.getStatus().name());
                    ps.setObject(4, newWarehouse.getManagerId());
                    ps.setObject(5, newWarehouse.getCreatedAt());
                    ps.setObject(6, newWarehouse.getUpdatedAt());
                    return ps;
                },
                keyHolder
        );

        if (keyHolder.getKey() != null) {
            newWarehouse.setId(keyHolder.getKey().intValue());
        }

        return newWarehouse;


    }

    @Override
    public Optional<WarehouseRespDto> getDetail(Integer id) {
        String sql = """
                SELECT w.id, w.name, w.address, w.status, u.first_name, u.last_name
                FROM warehouse w
                JOIN users u ON w.manager_id = u.user_id
                WHERE w.id = ?
                """;
        return Optional.of(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> toDto(rs, rowNum), id));
    }

    @Override
    public List<WarehouseRespDto> getAllDetail() {
        String sql = """
                SELECT w.id, w.name, w.address, w.status, u.first_name, u.last_name
                FROM warehouse w
                JOIN users u ON w.manager_id = u.user_id
                """;
        return jdbcTemplate.query(sql, this::toDto);
    }

    @Override
    public void activeById(Integer id) {
        String sql = """
                UPDATE warehouse 
                SET status = 'ACTIVE' 
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existById(Integer id) {

        String sql = "SELECT COUNT(1) FROM warehouse WHERE id = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        return count != null && count > 0;
    }

    private WarehouseRespDto toDto(ResultSet rs, int rowNum) throws SQLException {
        WarehouseRespDto dto = new WarehouseRespDto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("address"),
                WarehouseStatus.valueOf(rs.getString("status")) == WarehouseStatus.ACTIVE,
                rs.getString("first_name") + " " + rs.getString("last_name")
        );
        return dto;
    }


}
