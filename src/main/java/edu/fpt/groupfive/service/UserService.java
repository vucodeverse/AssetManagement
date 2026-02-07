package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    void createUser(UserCreateRequest request);
    void updateUser(UserUpdateRequest request);
    void removeUser(Integer id);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Integer id);
}
