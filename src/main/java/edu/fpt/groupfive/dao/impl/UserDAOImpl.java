package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
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
public class UserDAOImpl implements UserDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Optional<Users> findUserByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public void insert(Users users) {
        String query = """
                   INSERT INTO Users
                   (username, password_hash, full_name, phone_number, email,
                    status, role, department_id)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, users.getUsername());
            ps.setString(2, users.getPasswordHash());
            ps.setString(3, users.getFullName());
            ps.setString(4, users.getPhoneNumber());
            ps.setString(5, users.getEmail());
            ps.setString(6, users.getStatus());
            ps.setString(7, users.getRole().name());
            ps.setTimestamp(8, Timestamp.valueOf(users.getCreatedDate()));
            ps.setInt(9, users.getDepartmentId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Users users) {
        String query = """
            UPDATE Users
            SET
                password_hash = ?,
                full_name = ?,
                phone_number = ?,
                email = ?,
                status = ?,
                role = ?,
                updated_date = ?,
                department_id = ?
            WHERE user_id = ?
            """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, users.getPasswordHash());
            ps.setString(2, users.getFullName());
            ps.setString(3, users.getPhoneNumber());
            ps.setString(4, users.getEmail());
            ps.setString(5, users.getStatus());
            ps.setString(6, users.getRole().name());
            ps.setTimestamp(7, Timestamp.valueOf(users.getUpdatedDate()));
            ps.setInt(8, users.getDepartmentId());
            ps.setInt(9, users.getUserId());

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
                  SELECT 1 FROM Users
                           WHERE username = ? AND status = 'ACTIVE'
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
    public boolean existsByEmail(String email) {
        String query = """
                  SELECT 1 FROM Users WHERE email = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, email);

            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Users> findAll() {
        String query = """
                SELECT * FROM users WHERE status = 'ACTIVE'
                """;

        List<Users> list = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Users user = new Users();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                user.setRole(Role.valueOf(rs.getString("role")));

                Timestamp created = rs.getTimestamp("created_date");
                Timestamp updated = rs.getTimestamp("updated_date");

                if (created != null) user.setCreatedDate(created.toLocalDateTime());
                if (updated != null) user.setUpdatedDate(updated.toLocalDateTime());

                user.setDepartmentId(rs.getInt("department_id"));

                list.add(user);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public Optional<Users> findById(Integer id) {
        String query = """
            SELECT * FROM Users
            WHERE user_id = ? AND status = 'ACTIVE'
            """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Users u = new Users();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("full_name"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                u.setRole(Role.valueOf(rs.getString("role")));
                u.setDepartmentId(rs.getInt("department_id"));

                Timestamp created = rs.getTimestamp("created_date");
                Timestamp updated = rs.getTimestamp("updated_date");

                if (created != null) u.setCreatedDate(created.toLocalDateTime());
                if (updated != null) u.setUpdatedDate(updated.toLocalDateTime());

                return Optional.of(u);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }


}
