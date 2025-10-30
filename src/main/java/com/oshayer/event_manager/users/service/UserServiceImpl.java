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
import java.util.UUID;

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

    private void deleteOrgUserByRole(UUID userId, EnumUserRole expectedRole) {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("You cannot delete yourself.");
        }

        if (user.getOrganization() == null
                || currentAdmin.getOrganization() == null
                || !user.getOrganization().getId().equals(currentAdmin.getOrganization().getId())) {
            throw new IllegalArgumentException("User does not belong to your organization.");
        }

        if (user.getRole() != expectedRole) {
            throw new IllegalArgumentException("User role mismatch. Expected " + expectedRole.name());
        }

        userRepository.delete(user);
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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

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

        UserResponse response = new UserResponse();
        response.setId(newUser.getId());
        response.setFirstName(newUser.getFirstName());
        response.setLastName(newUser.getLastName());
        response.setFullName(newUser.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPhone(newUser.getPhone());
        response.setUsername(newUser.getUsername());
        response.setOrganizationId(newUser.getOrganization() != null
                ? newUser.getOrganization().getId()
                : null);

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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

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
        newUser.setRole(EnumUserRole.ROLE_OPERATOR);
        newUser.setOrganization(currentAdmin.getOrganization());
        newUser.setEmailVerified(true);
        newUser.setUsername(request.getUsername());

        userRepository.save(newUser);

        UserResponse response = new UserResponse();
        response.setId(newUser.getId());
        response.setFirstName(newUser.getFirstName());
        response.setLastName(newUser.getLastName());
        response.setFullName(newUser.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPhone(newUser.getPhone());
        response.setUsername(newUser.getUsername());
        response.setOrganizationId(newUser.getOrganization() != null
                ? newUser.getOrganization().getId()
                : null);

        if (newUser.getRole() != null) {
            response.setRoleCode(newUser.getRole().getCode());
            response.setRoleName(newUser.getRole().getDisplayName());
        }

        return response;
    }

    // ----------------------- NEW: Create Event Checker -----------------------
    @Override
    public UserResponse createEventChecker(EventCheckerCreateRequest request) {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        if (!currentAdmin.isEmailVerified()) {
            throw new RuntimeException("Admin email not verified");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

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
        newUser.setRole(EnumUserRole.ROLE_EVENT_CHECKER); // 👈 key role
        newUser.setOrganization(currentAdmin.getOrganization());
        newUser.setEmailVerified(true);
        newUser.setUsername(request.getUsername());

        userRepository.save(newUser);

        UserResponse response = new UserResponse();
        response.setId(newUser.getId());
        response.setFirstName(newUser.getFirstName());
        response.setLastName(newUser.getLastName());
        response.setFullName(newUser.getFullName());
        response.setEmail(newUser.getEmail());
        response.setPhone(newUser.getPhone());
        response.setUsername(newUser.getUsername());
        response.setOrganizationId(newUser.getOrganization() != null
                ? newUser.getOrganization().getId()
                : null);

        if (newUser.getRole() != null) {
            response.setRoleCode(newUser.getRole().getCode());
            response.setRoleName(newUser.getRole().getDisplayName());
        }

        return response;
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
                    response.setOrganizationId(user.getOrganization().getId());
                    response.setOrganizationName(user.getOrganization().getName());
                    response.setSignupDate(user.getSignupDate());
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
                    response.setOrganizationId(user.getOrganization().getId());
                    response.setOrganizationName(user.getOrganization().getName());
                    response.setSignupDate(user.getSignupDate());
                    return response;
                })
                .toList();
    }

    // ----------------------- NEW: List Event Checkers in Org -----------------
    @Override
    public List<UserResponse> getAllEventCheckersInOrg() {
        UserEntity currentAdmin = getCurrentUserEntity();
        if (currentAdmin == null) {
            throw new RuntimeException("No authenticated user found");
        }

        List<UserEntity> checkers = userRepository.findByOrganizationIdAndRole(
                currentAdmin.getOrganization().getId(),
                EnumUserRole.ROLE_EVENT_CHECKER
        );

        return checkers.stream()
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
                    response.setOrganizationId(user.getOrganization().getId());
                    response.setOrganizationName(user.getOrganization().getName());
                    response.setSignupDate(user.getSignupDate());
                    return response;
                })
                .toList();
    }

    @Override
    public void deleteEventManager(UUID userId) {
        deleteOrgUserByRole(userId, EnumUserRole.ROLE_EVENT_MANAGER);
    }

    @Override
    public void deleteOperator(UUID userId) {
        deleteOrgUserByRole(userId, EnumUserRole.ROLE_OPERATOR);
    }

    @Override
    public void deleteEventChecker(UUID userId) {
        deleteOrgUserByRole(userId, EnumUserRole.ROLE_EVENT_CHECKER);
    }
}
