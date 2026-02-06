package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.model.Users;

import java.util.List;

public interface UserService {
    void createUser(UseCreateRequest request);
    List<Users> getAllUsers();
    Integer getUserIdByUsername(String username);
}
