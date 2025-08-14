package com.oshayer.event_manager.users.service;

import com.oshayer.event_manager.users.dto.UpdateUserRequest;
import com.oshayer.event_manager.users.dto.OrgUserCreateRequest;
import com.oshayer.event_manager.users.dto.UserResponse;
import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import com.oshayer.event_manager.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserResponse getCurrentUserProfile() {
        UserEntity user = getCurrentAuthenticatedUser(); // SecurityContext
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse updateProfile(UpdateUserRequest request) {
        UserEntity user = getCurrentAuthenticatedUser();
        modelMapper.map(request, user);
        userRepository.save(user);
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse orgAdminCreateUser(OrgUserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        UserEntity user = modelMapper.map(request, UserEntity.class);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (user.getRole() == null) {
            user.setRole(EnumUserRole.ROLE_OPERATOR);
        }
        userRepository.save(user);
        return modelMapper.map(user, UserResponse.class);
    }

    private UserEntity getCurrentAuthenticatedUser() {
        // TODO: Implement SecurityContext fetch
        return null;
    }
}
