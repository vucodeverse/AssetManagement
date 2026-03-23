package edu.fpt.groupfive.service;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;

import edu.fpt.groupfive.model.Users;

import java.util.List;
import java.util.Map;

public interface UserService {
    void createUser(UserCreateRequest request);

    void updateUser(UserUpdateRequest request);

    void removeUser(Integer id);
    Integer getUserIdByUsername(String username);

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

    boolean existsByEmail(String email, Integer userId);

    boolean existsByPhone(String phone, Integer userId);

    boolean existsManager(Integer departmentId, Integer userId);

    boolean existsDirector(Integer userId);

    Map<Integer, String> getUserIdToUsernameMap();
    List<UserResponse> findAll();
    Users findByUsername(String username);

    Users findById(Integer id);

}
