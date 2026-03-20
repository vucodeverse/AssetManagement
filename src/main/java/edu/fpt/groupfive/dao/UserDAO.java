package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    Optional<User> findUserByUsername(String username);

    void insert(User user);

    void update(User user);

    void delete(Integer id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAll();

    Optional<User> findById(int userId);
}
