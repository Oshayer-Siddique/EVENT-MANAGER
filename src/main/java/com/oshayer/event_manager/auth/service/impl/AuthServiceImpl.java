package com.oshayer.event_manager.auth.service.impl;

import com.oshayer.event_manager.auth.dto.*;
import com.oshayer.event_manager.auth.security.JwtTokenProvider;
import com.oshayer.event_manager.auth.service.AuthService;
import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        UserEntity user = modelMapper.map(request, UserEntity.class);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(EnumUserRole.ROLE_USER);
        user.setEmailVerified(false);
        userRepository.save(user);

        // TODO: Send email verification link with token
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return JwtResponse.builder().token(token).build();
    }

    @Override
    public void verifyEmail(String token) {
        // TODO: decode token, find user, set emailVerified = true
    }
}
