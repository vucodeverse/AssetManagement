package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;

import edu.fpt.groupfive.model.Users;

import java.util.List;

public interface UserService {
    void createUser(UseCreateRequest request);
    List<Users> getAllUsers();
    List<UserResponse> getAllUsers2();
    Integer getUserIdByUsername(String username);
    String findNameById(Integer userId);
    void createUser(UserCreateRequest request);
    void updateUser(UserUpdateRequest request);
    void removeUser(Integer id);
    List<UserResponse> searchUsers(
            int page, int size,
            String status, Integer departmentId,
            Role role, String keyword);

    int getTotalPagesWithFilter(
            int size,
            String status, Integer departmentId,
            Role role, String keyword);
    UserResponse getUserById(Integer id);
    List<UserResponse> getAllUserByDepartId(Integer departId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);


}
