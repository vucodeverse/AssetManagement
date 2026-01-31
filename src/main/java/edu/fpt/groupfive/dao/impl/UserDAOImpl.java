package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.config.DatabaseConfig;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<Users> findUserByUsername(String username) {
        return null;
    }

    @Override
    public void insert(Users users) {
        String query = """
                   INSERT INTO Users
                   (username, password_hash, full_name, phone_number, email,
                    status, role, created_date, department_id)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setString(1, users.getUsername());
            preparedStatement.setString(2, users.getPasswordHash());
            preparedStatement.setString(3, users.getFullName());
            preparedStatement.setString(4, users.getEmail());
            preparedStatement.setString(5, users.getPhoneNumber());
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

    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    public boolean existsByUsername(String username) {
        String query = """
                  SELECT 1 FROM Users WHERE username = ?
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, username);

            return preparedStatement.executeQuery().next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
