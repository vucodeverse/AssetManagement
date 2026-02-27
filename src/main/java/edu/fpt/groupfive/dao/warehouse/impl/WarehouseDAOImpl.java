package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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


}
