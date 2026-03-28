package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.common.UserStatus;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
import lombok.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final DatabaseConfig databaseConfig;

    // Map ResultSet sang Users object
    private Users mapRowToUser(ResultSet rs) throws Exception {
        Users user = new Users();

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setEmail(rs.getString("email"));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setDepartmentId(rs.getInt("department_id"));

        Timestamp created = rs.getTimestamp("created_date");
        Timestamp updated = rs.getTimestamp("updated_date");

        if (created != null) user.setCreatedDate(created.toLocalDateTime());
        if (updated != null) user.setUpdatedDate(updated.toLocalDateTime());

        return user;
    }

    // Thực thi truy vấn không có tham số và trả về danh sách Người dùng
    @NonNull
    private List<Users> getUsers(String query) {
        List<Users> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    // Tìm kiếm username trong database
    @Override
    public Optional<Users> findUserByUsername(String username) {
        String query = """
                  select * from Users where username = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Chèn một user mới vào database
    @Override
    public void insert(Users users) {
        String query = """
                   INSERT INTO Users
                   (username, password_hash, first_name, last_name,
                    phone_number, email,
                    status, role, department_id)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, users.getUsername());
            ps.setString(2, users.getPasswordHash());
            ps.setString(3, users.getFirstName());
            ps.setString(4, users.getLastName());
            ps.setString(5, users.getPhoneNumber());
            ps.setString(6, users.getEmail());
            ps.setString(7, users.getStatus().name());
            ps.setString(8, users.getRole().name());
            ps.setInt(9, users.getDepartmentId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                users.setUserId(rs.getInt(1));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Users users) {
        String query = """
                UPDATE Users
                SET
                    password_hash = ?, first_name = ?, last_name = ?,
                    phone_number = ?, email = ?, status = ?,
                    role = ?, updated_date = ?, department_id = ?
                WHERE user_id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, users.getPasswordHash());
            ps.setString(2, users.getFirstName());
            ps.setString(3, users.getLastName());
            ps.setString(4, users.getPhoneNumber());
            ps.setString(5, users.getEmail());
            ps.setString(6, users.getStatus().name());
            ps.setString(7, users.getRole().name());
            ps.setTimestamp(8, users.getUpdatedDate() != null ? Timestamp.valueOf(users.getUpdatedDate()) : null);
            ps.setInt(9, users.getDepartmentId());
            ps.setInt(10, users.getUserId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(Integer id) {
        String query = """
                UPDATE Users
                SET
                    status = 'INACTIVE',
                    updated_date = GETDATE()
                WHERE user_id = ?;
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setInt(1, id);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean existsByUsername(String username) {
        String query = """
                  SELECT 1 FROM Users WHERE username = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, username);

            return ps.executeQuery().next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByEmail(String email, Integer userId) {
        StringBuilder query = new StringBuilder("""
                  SELECT 1 FROM Users WHERE email = ?
                """);

        if (userId != null) {
            query.append(" AND user_id <> ?");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())
        ) {
            ps.setString(1, email);

            if (userId != null) {
                ps.setInt(2, userId);
            }
            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByPhone(String phone, Integer userId) {
        StringBuilder query = new StringBuilder("""
                SELECT 1 FROM Users WHERE phone_number = ?
                """);

        if (userId != null) {
            query.append(" AND user_id <> ?");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())
        ) {
            ps.setString(1, phone);

            if (userId != null) {
                ps.setInt(2, userId);
            }
            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean existsManagerByDepartment(Integer departmentId, Integer userId) {

        StringBuilder query = new StringBuilder("""
                    SELECT 1 FROM Users
                    WHERE department_id = ?
                    AND role = 'DEPARTMENT_MANAGER'
                """);

        // Nếu userId khác null, ta loại trừ chính user đó ra & sẽ dùng khi Update
        if (userId != null) {
            query.append(" AND user_id <> ?");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {

            ps.setInt(1, departmentId);
            if (userId != null) {
                ps.setInt(2, userId);
            }

            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exitsDirector(Role role, Integer userId) {

        StringBuilder query = new StringBuilder("""
                  SELECT 1 FROM Users
                  WHERE role = ?
                  AND status = 'ACTIVE'
                """);

        if (userId != null) {
            query.append(" AND user_id <> ?");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {

            ps.setString(1, role.name());

            if (userId != null) {
                ps.setInt(2, userId);
            }

            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean isAssetManagerLimit(Integer userId) {
        StringBuilder query = new StringBuilder("""
                SELECT COUNT(*) FROM Users
                WHERE role = 'ASSET_MANAGER'
                AND status = 'ACTIVE'
            """);

        if (userId != null) {
            query.append(" AND user_id <> ?");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {
            if (userId != null) {
                ps.setInt(1, userId);
            }
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) >= 2;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public List<Users> findAll() {
        String query = """
                SELECT * FROM users
                """;
        return getUsers(query);
    }

    @Override
    public Integer findUserIdByUsername(String username) {
        String sql = "select u.user_id from users u where u.username like ?";

        try (Connection connection = databaseConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public Optional<Users> findById(Integer id) {
        String query = """
                SELECT * FROM Users WHERE user_id = ?
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<Users> findByDepartmentId(Integer departmentId) {
        String query = """
                SELECT * FROM users
                         WHERE department_id = ?
                           AND status = 'ACTIVE'
                """;

        List<Users> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;

    }


    @Override
    public int countUsersInDepartment(Integer departmentId) {
        String sql = """
                    SELECT COUNT(*) FROM Users
                    WHERE department_id = ? AND status = 'ACTIVE'
                """;

        try (Connection c = databaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Users> searchUsers(int offset, int size, String status, Integer departmentId,
                                   Role role, String keyword) {
        StringBuilder query = new StringBuilder("""
                SELECT * FROM Users WHERE 1 = 1
                """);

        List<Object> param = new ArrayList<>();

        if (status != null) {
            query.append(" AND status = ?");
            param.add(status);
        }

        if (departmentId != null) {
            query.append(" AND department_id = ?");
            param.add(departmentId);
        }

        if (role != null) {
            query.append(" AND role = ?");
            param.add(role.name());
        }

        if (keyword != null) {
            query.append(" AND (username LIKE ? OR email LIKE ?)");
            param.add("%" + keyword + "%");
            param.add("%" + keyword + "%");
        }

        query.append(" ORDER BY user_id");
        query.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        param.add(offset);
        param.add(size);

        List<Users> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < param.size(); i++) {
                ps.setObject(i + 1, param.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }


    @Override
    public int countUsersWithFilter(String status, Integer departmentId, Role role, String keyword) {
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM Users
                    WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (departmentId != null) {
            sql.append(" AND department_id = ?");
            params.add(departmentId);
        }

        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }

        if (keyword != null) {
            sql.append(" AND (username LIKE ? OR email LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return 0;

    }

    @Override
    public String findFullNameByUserId(Integer userId) {
        String sql = "select first_name, last_name from users where user_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
            return "Unknown";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
