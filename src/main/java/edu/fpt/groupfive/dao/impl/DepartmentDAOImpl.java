package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.config.database.DatabaseConfig;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.model.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepartmentDAOImpl implements DepartmentDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(Department department) {
        String query = """
                    INSERT INTO Departments
                        (department_name, created_date, status, manager_user_id)
                    VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, department.getDepartmentName());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(department.getCreatedDate()));
            preparedStatement.setString(3, department.getStatus());
            preparedStatement.setObject(4, department.getManagerId());

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(Department department) {
        String sql = """
                    UPDATE Departments
                    SET department_name = ?,
                        updated_date = ?,
                        status = ?,
                        manager_user_id = ?
                    WHERE department_id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, department.getDepartmentName());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(department.getUpdatedDate()));
            preparedStatement.setString(3, department.getStatus());
            preparedStatement.setObject(4, department.getManagerId());
            preparedStatement.setInt(5, department.getDepartmentId());

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer departmentId) {
        String sql = """
                    UPDATE Departments 
                    SET WHERE status = 'INACTIVE',
                        updated_date = GETDATE() 
                    WHERE department_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setInt(1, departmentId);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Department> findById(Integer departmentId) {
        return Optional.empty();
    }

    @Override
    public List<Department> findAll() {
        String query = """
                SELECT * FROM Departments WHERE status = 'ACTIVE'
                """;

        List<Department> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()
        ){
            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setStatus(rs.getString("status"));
                d.setManagerId(rs.getInt("manager_user_id"));

                Timestamp created = rs.getTimestamp("created_date");
                Timestamp updated = rs.getTimestamp("updated_date");

                if (created != null) d.setCreatedDate(created.toLocalDateTime());
                if (updated != null) d.setUpdatedDate(updated.toLocalDateTime());

                list.add(d);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public boolean existsByName(String departmentName) {
        String sql = "SELECT 1 FROM Departments WHERE department_name = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, departmentName);
            return ps.executeQuery().next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
