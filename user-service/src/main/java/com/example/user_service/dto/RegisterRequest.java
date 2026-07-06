package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

    @Email
    String email,

    @NotBlank
    String username,

    @NotBlank
    String password,

    String firstName,

    String lastName
) {}