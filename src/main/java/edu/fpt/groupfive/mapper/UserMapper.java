package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UseCreateRequest request);
}







