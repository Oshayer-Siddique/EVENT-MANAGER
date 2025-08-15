package com.oshayer.event_manager.users.service;

import com.oshayer.event_manager.organizations.repository.OrganizationRepository;
import com.oshayer.event_manager.users.dto.*;
import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.oshayer.event_manager.auth.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final OrganizationRepository organizationRepository;

    @Override
    public UserResponse getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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




    private UserEntity getCurrentUserEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        return null;
    }


    @Override
    public UserResponse createEventManager(CreateEventManagerRequest request) {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        if (!currentAdmin.isEmailVerified()) {
            throw new RuntimeException("Admin email not verified");
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setFullName(request.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(EnumUserRole.ROLE_EVENT_MANAGER);
        newUser.setOrganization(currentAdmin.getOrganization());
        newUser.setEmailVerified(true);

        userRepository.save(newUser);
        return modelMapper.map(newUser, UserResponse.class);
    }


    @Override
    public UserResponse createOperator(OperatorCreateRequest request) {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        if (!currentAdmin.isEmailVerified()) {
            throw new RuntimeException("Admin email not verified");
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setFullName(request.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(EnumUserRole.ROLE_OPERATOR);
        newUser.setOrganization(currentAdmin.getOrganization());
        newUser.setEmailVerified(true); // ✅ auto verify

        userRepository.save(newUser);
        return modelMapper.map(newUser, UserResponse.class);
    }




}
