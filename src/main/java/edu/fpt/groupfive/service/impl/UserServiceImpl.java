package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.mapper.UserMapper;
import edu.fpt.groupfive.model.User;
import edu.fpt.groupfive.service.UserService;
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

    @Override
    public List<UserResponse> findAll() {
            return userDAO.findAll()
                    .stream()
                    .map(user -> new UserResponse(
                            user.getUserId(),
                            user.getFirstName() + " " + user.getLastName()
                    ))
                    .toList();
        }
    }

