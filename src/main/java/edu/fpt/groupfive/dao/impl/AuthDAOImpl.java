package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.AuthDAO;
import edu.fpt.groupfive.model.Users;

import java.util.Optional;

public class AuthDAOImpl implements AuthDAO {
    @Override
    public boolean isUserNameExists(String userName) {
        String sql = "";
        return false;
    }


    @Override
    public Optional<Users> getUserDetail(String username, String passwordHash) {
        return null;
    }
}
