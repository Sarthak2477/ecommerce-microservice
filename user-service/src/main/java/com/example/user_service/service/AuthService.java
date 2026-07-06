package com.example.user_service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.AuthResponse;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.entity.User;
import com.example.user_service.error.EmailAlreadyExistsException;
import com.example.user_service.error.InvalidCredentialsException;
import com.example.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.user_service.entity.Role;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException("The email " + registerRequest.email() + " already exists.");
        }

        User user = User.builder()
                .email(registerRequest.email())
                .username(registerRequest.username())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        if(user != null){
            userRepository.save(user);
        }
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                user.getId(), token, "Bearer");

    }

    public AuthResponse login(LoginRequest loginRequest){
        User user = userRepository
                    .findByEmail(loginRequest.email())
                    .orElseThrow(()->new InvalidCredentialsException("Invalid Exception"));
        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(user.getId(), token, "Bearer");
    }

}
