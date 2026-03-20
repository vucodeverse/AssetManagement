package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.model.User;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();
}
