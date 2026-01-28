package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AuthDAO;
import edu.fpt.groupfive.model.User;

public class AuthDAOImpl implements AuthDAO {
    @Override
    public boolean isUserNameExists(String userName) {
        String sql = "";
        return false;
    }

    @Override
    public User authenticateUser(String username, String passwordHash) {
        return null;
    }
}
