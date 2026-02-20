package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    Users toUser(UseCreateRequest request);

    UserResponse toUserResponse(Users user);
}
