package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<Users> findUserByUsername(String username);
    Users createUser(Users users);
}
