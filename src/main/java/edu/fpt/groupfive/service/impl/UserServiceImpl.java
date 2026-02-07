package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.mapper.UserMapper;
import edu.fpt.groupfive.model.Users;
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
    private final PasswordEncoder passwordEncoder;


    @Override
    public void createUser(UserCreateRequest request) {
        if (userDAO.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (request.getEmail() != null &&
                userDAO.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Users user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedDate(LocalDateTime.now());

        userDAO.insert(user);
    }

    @Override
    public void updateUser(UserUpdateRequest request) {
        //Lấy user theo id
        Users existing = userDAO.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Kiểm tra email có tồn tại chưa
        if (request.getEmail() != null
                && !request.getEmail().equals(existing.getEmail())
                && userDAO.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userMapper.updateUserFromRequest(request, existing);

        // Chỉ đổi password nếu nhập mới
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        existing.setUpdatedDate(LocalDateTime.now());
        userDAO.update(existing);
    }


    @Override
    public void removeUser(Integer id) {
        Users user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userDAO.delete(id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<Users> users = userDAO.findAll();
        return userMapper.toResponseList(users);
    }

    @Override
    public UserResponse getUserById(Integer id) {
        Users user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }


}
