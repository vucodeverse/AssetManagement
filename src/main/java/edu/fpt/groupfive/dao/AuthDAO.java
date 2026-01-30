package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Users;

import java.util.Optional;

public interface AuthDAO{
    boolean isUserNameExists(String userName);
    Optional<Users> getUserDetail(String username, String passwordHash);
}
