package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<Users> findUserByUsername(String username);
    void insert (Users users);
    void update (Users users);
    void delete (Integer id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Users> findAll();
    Integer findUserIdByUsername(String username);
}
