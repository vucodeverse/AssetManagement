package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.model.Department;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Repository
public class DepartmentDAOImpl implements DepartmentDAO {

    private final DataSource dataSource;

    public  DepartmentDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Department save(Department department) {
        return null;
    }
}
