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
}
