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

import java.util.List;

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

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl())
                .address(user.getAddress())
                .role(user.getRole())
                .roleCode(user.getRoleCode())
                .roleName(user.getRoleName())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .emailVerified(user.isEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .mobileVerified(user.isMobileVerified())
                .mobileVerifiedAt(user.getMobileVerifiedAt())
                .signupDate(user.getSignupDate())
                .signupAt(user.getSignupAt())
                .lastLoginAt(user.getLastLoginAt())
                .lastLogoutAt(user.getLastLogoutAt())
                .totalTicketCount(user.getTotalTicketCount())
                .totalTicketPrice(user.getTotalTicketPrice())
                .ticketsUsed(user.getTicketsUsed())
                .ticketsAtHand(user.getTicketsAtHand())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


    @Override
    public UserResponse updateProfile(UpdateUserRequest request) {
        UserEntity user = getCurrentAuthenticatedUser();

        // update only allowed fields
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getImageUrl() != null) {
            user.setImageUrl(request.getImageUrl());
        }

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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        } else {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
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

        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new UserEntity
        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setFullName(request.getFullName() != null
                ? request.getFullName()
                : request.getFirstName() + " " + request.getLastName());
        newUser.setImageUrl(request.getImageUrl());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(EnumUserRole.ROLE_EVENT_MANAGER);
        newUser.setOrganization(currentAdmin.getOrganization());
        newUser.setEmailVerified(true);
        newUser.setUsername(request.getUsername());

        userRepository.save(newUser);

        // Manually map to UserResponse
        UserResponse response = new UserResponse();
        response.setId(newUser.getId());
        response.setFirstName(newUser.getFirstName());
        response.setLastName(newUser.getLastName());
        response.setFullName(newUser.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPhone(newUser.getPhone());
//        response.setImageUrl(newUser.getImageUrl());
        response.setUsername(newUser.getUsername());
        response.setOrganizationId(newUser.getOrganization() != null
                ? newUser.getOrganization().getId()
                : null);

        // Manually set role fields
        if (newUser.getRole() != null) {
            response.setRoleCode(newUser.getRole().getCode());
            response.setRoleName(newUser.getRole().getDisplayName());
        }

        return response;
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



    @Override
    public List<UserResponse> getAllEventManagersInOrg() {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        List<UserEntity> managers = userRepository.findByOrganizationIdAndRole(
                currentAdmin.getOrganization().getId(),
                EnumUserRole.ROLE_EVENT_MANAGER
        );

        return managers.stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setFirstName(user.getFirstName());
                    response.setLastName(user.getLastName());
                    response.setFullName(user.getFullName());
                    response.setEmail(user.getEmail());
                    response.setPhone(user.getPhone());
                    response.setImageUrl(user.getImageUrl());
                    response.setUsername(user.getUsername());
                    response.setRoleCode(user.getRole().getCode());
                    response.setRoleName(user.getRole().getDisplayName());
                    response.setEmailVerified(user.isEmailVerified());
                    return response;
                })
                .toList();
    }

    @Override
    public List<UserResponse> getAllOperatorsInOrg() {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        List<UserEntity> operators = userRepository.findByOrganizationIdAndRole(
                currentAdmin.getOrganization().getId(),
                EnumUserRole.ROLE_OPERATOR
        );

        return operators.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .toList();
    }





}
