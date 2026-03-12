package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.util.config.database.DatabaseConfig;
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

import static java.sql.Types.INTEGER;

@Repository
@RequiredArgsConstructor
public class DepartmentDAOImpl implements DepartmentDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void insert(Department department) {
        String query = """
                    INSERT INTO Departments
                        (department_name, description,
                         status, manager_user_id)
                    VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getDescription());
            ps.setString(3, department.getStatus());
            ps.setObject(4, department.getManagerId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(Department department) {
        String sql = """
                    UPDATE Departments
                    SET department_name = ?,
                        description = ?,
                        updated_date = ?,
                        status = ?,
                        manager_user_id = ?
                    WHERE department_id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getDescription());
            ps.setTimestamp(3, department.getUpdatedDate() != null ? Timestamp.valueOf(department.getUpdatedDate()) : null);
            ps.setString(4, department.getStatus());
            ps.setObject(5, department.getManagerId());
            ps.setInt(6, department.getDepartmentId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer departmentId) {
        String sql = """
                    UPDATE Departments
                    SET status = 'INACTIVE',
                        updated_date = GETDATE()
                    WHERE department_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, departmentId);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public Optional<Department> findById(Integer departmentId) {
//        String query = """
//                SELECT * FROM Departments
//                WHERE department_id = ? AND status = 'ACTIVE'
//                """;
//
//        try (Connection connection = databaseConfig.getConnection();
//             PreparedStatement ps = connection.prepareStatement(query)
//        ) {
//            ps.setInt(1, departmentId);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//                Department department = new Department();
//                department.setDepartmentId(rs.getInt("department_id"));
//                department.setDepartmentName(rs.getString("department_name"));
//                department.setDescription(rs.getString("description"));
//                department.setStatus(rs.getString("status"));
//
//                Timestamp created = rs.getTimestamp("created_date");
//                Timestamp updated = rs.getTimestamp("updated_date");
//
//                if (created != null)
//                    department.setCreatedDate(created.toLocalDateTime());
//                if (updated != null)
//                    department.setUpdatedDate(updated.toLocalDateTime());
//
//                department.setManagerId(rs.getInt("manager_user_id"));
//
//                return Optional.of(department);
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return Optional.empty();
//    }

    @Override
    public Optional<Department> findById(Integer departmentId) {

        String query = """
                SELECT d.*, u.first_name, u.last_name
                FROM Departments d
                LEFT JOIN Users u
                    ON d.manager_user_id = u.user_id
                WHERE d.department_id = ?
                AND d.status = 'ACTIVE'
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Department department = new Department();

                department.setDepartmentId(rs.getInt("department_id"));
                department.setDepartmentName(rs.getString("department_name"));
                department.setDescription(rs.getString("description"));
                department.setStatus(rs.getString("status"));

                Integer managerId = rs.getObject("manager_user_id", Integer.class);
                department.setManagerId(managerId);

                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                if (firstName != null) {
                    department.setManagerName(firstName + " " + lastName);
                } else {
                    department.setManagerName("No Manager");
                }

                Timestamp created = rs.getTimestamp("created_date");
                Timestamp updated = rs.getTimestamp("updated_date");

                if (created != null)
                    department.setCreatedDate(created.toLocalDateTime());
                if (updated != null)
                    department.setUpdatedDate(updated.toLocalDateTime());

                return Optional.of(department);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }


    @Override
    public List<Department> findAll() {
        String query = """
                SELECT * FROM Departments WHERE status = 'ACTIVE'
                """;

        List<Department> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
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
        String query = """
                SELECT 1 FROM Departments
                WHERE department_name = ? AND status = 'ACTIVE'
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, departmentName);
            return ps.executeQuery().next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Department> searchByName(String keyword) {
        String sql = """
                    SELECT * FROM Departments
                    WHERE status = 'ACTIVE'
                    AND department_name LIKE ?
                """;

        List<Department> list = new ArrayList<>();

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
                d.setStatus(rs.getString("status"));
                d.setManagerId(rs.getInt("manager_user_id"));

                list.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

//    @Override
//    public List<Department> findAllPaged(int page, int size) {
//        int offset = (page - 1) * size;
//
//        String sql = """
//                    SELECT * FROM Departments
//                    WHERE status = 'ACTIVE'
//                    ORDER BY department_id
//                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
//                """;
//
//        List<Department> list = new ArrayList<>();
//
//        try (Connection c = databaseConfig.getConnection();
//             PreparedStatement ps = c.prepareStatement(sql)) {
//
//            ps.setInt(1, offset);
//            ps.setInt(2, size);
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                Department d = new Department();
//                d.setDepartmentId(rs.getInt("department_id"));
//                d.setDepartmentName(rs.getString("department_name"));
//                d.setDescription(rs.getString("description"));
//                d.setStatus(rs.getString("status"));
//                d.setManagerId(rs.getInt("manager_user_id"));
//                list.add(d);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return list;
//    }

    @Override
    public List<Department> findAllPaged(int page, int size) {
        int offset = (page - 1) * size;

        String sql = """
                    SELECT d.*, u.first_name, u.last_name
                    FROM Departments d LEFT JOIN Users u
                    ON d.manager_user_id = u.user_id
                    WHERE d.status = 'ACTIVE'
                    ORDER BY d.department_id
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        List<Department> list = new ArrayList<>();

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, offset);
            ps.setInt(2, size);

            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
                d.setStatus(rs.getString("status"));
                d.setManagerId(rs.getInt("manager_user_id"));

                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                if (firstName != null) {
                    d.setManagerName(firstName + " " + lastName);
                } else {
                    d.setManagerName("No Manager");
                }
                list.add(d);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public int countDepartments() {
        String sql = """
                    SELECT COUNT(*) FROM Departments
                    WHERE status = 'ACTIVE'
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateManager(Integer departmentId, Integer userId) {

        String sql = """
                UPDATE Departments
                SET manager_user_id = ?,
                    updated_date = GETDATE()
                WHERE department_id = ?
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, INTEGER);
            } else {
                ps.setInt(1, userId);
            }

            ps.setInt(2, departmentId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
