package com.transportation.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.transportation.booking.dto.request.UserCreationRequest;
import com.transportation.booking.dto.request.UserUpdateRequest;
import com.transportation.booking.dto.response.UserResponse;
import com.transportation.booking.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);

    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}