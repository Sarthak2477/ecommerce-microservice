package com.example.user_service.service;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    public UserResponse getUser(@NonNull UUID id){
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(()->new RuntimeException("No user found."));
    }
}
