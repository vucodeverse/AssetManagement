package edu.fpt.groupfive.dao.impl.warehouse;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.util.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WarehouseDAOImpl implements WarehouseDAO {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Warehouse> rowMapper = (rs, rowNum) -> {
        Warehouse w = new Warehouse();
        w.setId(rs.getInt("id"));
        w.setName(rs.getString("name"));
        w.setAddress(rs.getString("address"));
        w.setManagerUserId(rs.getInt("manager_user_id"));
        w.setStatus(ActiveStatus.valueOf(rs.getString("status")));
        return w;
    };

    @Override
    public List<Warehouse> findAll() {
        String sql = "SELECT id, name, address, manager_user_id, status FROM wh_warehouses";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Warehouse getById(Integer id) {
        String sql = "SELECT id, name, address, manager_user_id, status FROM wh_warehouses WHERE id = ?";
        List<Warehouse> list = jdbcTemplate.query(sql, rowMapper, id);
        if (list.isEmpty()) {
            throw new WarehouseNotFoundException(id);
        }
        return list.get(0);
    }

    @Override
    public Warehouse getByManager(Integer userId) {
        String sql = "SELECT id, name, address, manager_user_id, status FROM wh_warehouses WHERE manager_user_id = ?";
        List<Warehouse> list = jdbcTemplate.query(sql, rowMapper, userId);
        if (list.isEmpty()) {
            throw new WarehouseNotFoundException("Không tìm thấy kho hàng cho người quản lý ID: " + userId);
        }
        return list.get(0);
    }

    @Override
    public Warehouse create(Warehouse warehouse) {
        String sql = "INSERT INTO wh_warehouses (name, address, manager_user_id, status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, warehouse.getName());
            ps.setString(2, warehouse.getAddress());
            ps.setInt(3, warehouse.getManagerUserId());
            ps.setString(4, warehouse.getStatus() != null ? warehouse.getStatus().name() : ActiveStatus.ACTIVE.name());
            return ps;
        }, keyHolder);
        warehouse.setId(keyHolder.getKey().intValue());
        return warehouse;
    }

    @Override
    public Warehouse update(Warehouse warehouse) {
        String sql = "UPDATE wh_warehouses SET name = ?, address = ?, manager_user_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getManagerUserId(),
                warehouse.getId());
        return warehouse;
    }

    @Override
    public void setActiveStatus(Integer id, ActiveStatus status) {
        String sql = "UPDATE wh_warehouses SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }
}
