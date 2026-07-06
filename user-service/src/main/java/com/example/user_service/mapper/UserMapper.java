package com.example.user_service.mapper;

import org.mapstruct.Mapper;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
