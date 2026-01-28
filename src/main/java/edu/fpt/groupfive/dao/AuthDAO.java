package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.User;

public interface AuthDAO{
    boolean isUserNameExists(String userName);
    User authenticateUser(String username,String passwordHash);
}
