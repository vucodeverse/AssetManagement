package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhWarehouseDAO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WhWarehouseDAOImpl implements WhWarehouseDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Warehouse> warehouseMapper = (rs, rowNum) -> {
        Warehouse w = new Warehouse();
        w.setWarehouseId(rs.getInt("warehouse_id"));
        w.setName(rs.getString("name"));
        w.setAddress(rs.getString("address"));
        w.setManagerUserId(rs.getInt("manager_user_id"));
        w.setStatus(rs.getString("status"));
        return w;
    };

    @Override
    public List<Warehouse> getAllWarehouses() {
        return jdbcTemplate.query("SELECT * FROM wh_warehouses", warehouseMapper);
    }

    @Override
    public Optional<Warehouse> getWarehouseById(int warehouseId) {
        List<Warehouse> results = jdbcTemplate.query("SELECT * FROM wh_warehouses WHERE warehouse_id = ?", warehouseMapper, warehouseId);
        return results.stream().findFirst();
    }

    @Override
    public void createWarehouse(Warehouse warehouse) {
        String sql = "INSERT INTO wh_warehouses (name, address, manager_user_id, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, warehouse.getName(), warehouse.getAddress(), warehouse.getManagerUserId(), warehouse.getStatus());
    }

    @Override
    public boolean existsAny() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wh_warehouses", Integer.class);
        return count != null && count > 0;
    }
}
