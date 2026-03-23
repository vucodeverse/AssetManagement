package edu.fpt.groupfive.dao;

import java.util.Optional;

public interface AuthDAO{
    boolean isUserNameExists(String userName);
//    Optional<Users> getUserDetail(String username, String passwordHash);
}
