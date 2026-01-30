package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<Users> findUserByUsername(String username) {
        return null;
    }
}
