package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.User;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public Optional<User> findUserByUsername(String username) {

        String query = """
            SELECT user_id, username, password_hash, first_name, last_name,
                   phone_number, email, status, role,
                   created_date, updated_date, department_id
            FROM users
            WHERE username = ?
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(User user) {

        String query = """
            INSERT INTO users
            (username, password_hash, first_name, last_name,
             phone_number, email, status, role, created_date, department_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getStatus());
            ps.setString(8, user.getRole());
            ps.setTimestamp(9, Timestamp.valueOf(user.getCreatedDate()));
            ps.setInt(10, user.getDepartmentId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {

        String query = """
            UPDATE users
            SET password_hash = ?,
                first_name = ?,
                last_name = ?,
                phone_number = ?,
                email = ?,
                status = ?,
                role = ?,
                updated_date = ?,
                department_id = ?
            WHERE user_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, user.getPasswordHash());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getStatus());
            ps.setString(7, user.getRole());
            ps.setTimestamp(8, Timestamp.valueOf(user.getUpdatedDate()));
            ps.setInt(9, user.getDepartmentId());
            ps.setInt(10, user.getUserId());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {

        String query = """
            UPDATE users
            SET status = 'INACTIVE',
                updated_date = GETDATE()
            WHERE user_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {

        String query = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {

        String query = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);

            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll() {

        String query = """
            SELECT user_id, username, password_hash, first_name, last_name,
                   phone_number, email, status, role,
                   created_date, updated_date, department_id
            FROM users
            WHERE status = 'ACTIVE'
        """;

        List<User> list = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public Optional<User> findById(int userId) {

        String query = """
            SELECT user_id, username, password_hash, first_name, last_name,
                   phone_number, email, status, role,
                   created_date, updated_date, department_id
            FROM users
            WHERE user_id = ?
        """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {

        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));
        user.setRole(rs.getString("role"));

        Timestamp created = rs.getTimestamp("created_date");
        Timestamp updated = rs.getTimestamp("updated_date");

        if (created != null) {
            user.setCreatedDate(created.toLocalDateTime());
        }

        if (updated != null) {
            user.setUpdatedDate(updated.toLocalDateTime());
        }

        user.setDepartmentId(rs.getInt("department_id"));

        return user;
    }
}