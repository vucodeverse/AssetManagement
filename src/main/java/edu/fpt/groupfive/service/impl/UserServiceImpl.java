package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.mapper.UserMapper;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UseCreateRequest request) {
        if (userDAO.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedDate(LocalDateTime.now());

        userDAO.insert(user);
    }

    @Override
    public List<Users> getAllUsers() {
        return userDAO.findAll();
    }
}
