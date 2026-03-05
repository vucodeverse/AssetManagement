package edu.fpt.groupfive.dao.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class WarehouseDAOImpl implements WarehouseDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Warehouse> rowMapper = new RowMapper<Warehouse>() {
        @Override
        public Warehouse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Warehouse.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .address(rs.getString("address"))
                    .managerUserId(rs.getInt("manager_user_id"))
                    .status(rs.getString("status"))
                    .build();
        }
    };

    @Override
    public Warehouse findById(Integer id) {
        String sql = "SELECT * FROM wh_warehouse WHERE id = ?";
        List<Warehouse> result = jdbcTemplate.query(sql, rowMapper, id);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Warehouse> findAll() {
        String sql = "SELECT * FROM wh_warehouse ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public int insert(Warehouse warehouse) {
        String sql = "INSERT INTO wh_warehouse (name, address, manager_user_id, status) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, warehouse.getName(), warehouse.getAddress(), warehouse.getManagerUserId(),
                warehouse.getStatus());
    }

    @Override
    public int update(Warehouse warehouse) {
        String sql = "UPDATE wh_warehouse SET name = ?, address = ?, manager_user_id = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, warehouse.getName(), warehouse.getAddress(), warehouse.getManagerUserId(),
                warehouse.getStatus(), warehouse.getId());
    }
}
