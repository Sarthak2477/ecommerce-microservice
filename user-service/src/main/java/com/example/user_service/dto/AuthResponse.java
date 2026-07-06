package com.example.user_service.dto;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    String accessToken,
    String tokenType
){

}
