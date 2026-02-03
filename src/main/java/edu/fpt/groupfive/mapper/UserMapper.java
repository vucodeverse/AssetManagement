package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.model.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UseCreateRequest request);
}
