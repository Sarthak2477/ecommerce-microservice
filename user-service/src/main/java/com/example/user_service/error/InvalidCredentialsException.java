package com.example.user_service.error;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message){
        super(message);
    }
}
