package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseDAOImpl implements WarehouseDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Warehouse> rowMapper = (rs, rowNum) -> new Warehouse(
            rs.getInt("warehouse_id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getInt("manager_user_id"),
            rs.getString("status")
    );

    @Override
    public void insert(Warehouse warehouse) {
        String sql = "INSERT INTO wh_warehouses (name, address, manager_user_id, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, warehouse.getName(), warehouse.getAddress(), warehouse.getManagerUserId(), warehouse.getStatus());
    }

    @Override
    public void update(Warehouse warehouse) {
        String sql = "UPDATE wh_warehouses SET name = ?, address = ?, manager_user_id = ?, status = ? WHERE warehouse_id = ?";
        jdbcTemplate.update(sql, warehouse.getName(), warehouse.getAddress(), warehouse.getManagerUserId(), warehouse.getStatus(), warehouse.getWarehouseId());
    }

    @Override
    public Optional<Warehouse> findById(Integer warehouseId) {
        String sql = "SELECT * FROM wh_warehouses WHERE warehouse_id = ?";
        List<Warehouse> results = jdbcTemplate.query(sql, rowMapper, warehouseId);
        return results.stream().findFirst();
    }

    @Override
    public List<Warehouse> findAll() {
        String sql = "SELECT * FROM wh_warehouses";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
