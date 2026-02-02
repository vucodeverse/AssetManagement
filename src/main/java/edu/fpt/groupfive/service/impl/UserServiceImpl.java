package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Override
    public void createUser(UseCreateRequest request) {
        if (userDAO.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setStatus("ACTIVE");
        user.setCreatedDate(LocalDateTime.now());
        user.setDepartmentId(request.getDepartmentId());

        userDAO.insert(user);
    }
}
