package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.model.Users;
import org.mapstruct.*;


import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreateRequest request);

    @Mapping(target = "passwordHash", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget Users user);

    UserResponse toResponse(Users user);

    List<UserResponse> toResponseList(List<Users> users);
}
