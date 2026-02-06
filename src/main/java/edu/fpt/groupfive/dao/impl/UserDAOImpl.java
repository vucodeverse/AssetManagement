package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
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
                Users u = new Users();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("fullname"));
                u.setPhoneNumber(rs.getString("phone"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                u.setRole(rs.getString("role"));

                Timestamp created = rs.getTimestamp("created_at");
                Timestamp updated = rs.getTimestamp("updated_at");

                if (created != null) u.setCreatedDate(created.toLocalDateTime());
                if (updated != null) u.setUpdatedDate(updated.toLocalDateTime());

                return Optional.of(u);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(Users users) {
        String query = """
                   INSERT INTO Users
                   (username, password_hash, full_name, phone_number, email,
                    status, role, created_date, department_id)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setString(1, users.getUsername());
            preparedStatement.setString(2, users.getPasswordHash());
            preparedStatement.setString(3, users.getFullName());
            preparedStatement.setString(4, users.getPhoneNumber());
            preparedStatement.setString(5, users.getEmail());
            preparedStatement.setString(6, users.getStatus());
            preparedStatement.setString(7, users.getRole());
            preparedStatement.setTimestamp(8, Timestamp.valueOf(users.getCreatedDate()));
            preparedStatement.setInt(9, users.getDepartmentId());

            preparedStatement.executeUpdate();
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
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, users.getPasswordHash());
            preparedStatement.setString(2, users.getFullName());
            preparedStatement.setString(3, users.getPhoneNumber());
            preparedStatement.setString(4, users.getEmail());
            preparedStatement.setString(5, users.getStatus());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(users.getCreatedDate()));
            preparedStatement.setInt(7, users.getDepartmentId());
            preparedStatement.setInt(8, users.getUserId());

            preparedStatement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(Integer id) {
        String query = """
                UPDATE Users
                SET status = 'INACTIVE', updated_date = GETDATE()
                WHERE user_id = ?;
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);

            preparedStatement.executeQuery().next();
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
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, username);

            return preparedStatement.executeQuery().next();
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
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, email);

            return preparedStatement.executeQuery().next();
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
                Users u = new Users();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("full_name"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                u.setRole(rs.getString("role"));

                Timestamp created = rs.getTimestamp("created_date");
                Timestamp updated = rs.getTimestamp("updated_date");

                if (created != null) u.setCreatedDate(created.toLocalDateTime());
                if (updated != null) u.setUpdatedDate(updated.toLocalDateTime());

                u.setDepartmentId(rs.getInt("department_id"));

                list.add(u);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public Integer findUserIdByUsername(String username) {
        String sql ="select u.user_id from users u where u.username = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){


            ps.setString(1, username);
            ResultSet rs= ps.executeQuery();
            if(rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


}
