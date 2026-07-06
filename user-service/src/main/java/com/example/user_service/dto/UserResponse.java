package com.example.user_service.dto;

import lombok.Builder;

@Builder
public record UserResponse( 
    String email,

    String username,

    String password,

    String firstName,

    String lastName) {}
