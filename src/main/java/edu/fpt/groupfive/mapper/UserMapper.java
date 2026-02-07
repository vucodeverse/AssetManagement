package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreateRequest request);
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget Users user);
    UserResponse toResponse(Users user);
    List<UserResponse> toResponseList(List<Users> users);
}
