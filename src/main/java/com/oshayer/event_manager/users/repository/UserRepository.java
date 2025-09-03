package com.oshayer.event_manager.users.repository;

import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username); // ✅ Add this

    Optional<UserEntity> findByEmailVerificationToken(String token);
    Optional<UserEntity> findByResetPasswordToken(String token);
    List<UserEntity> findByOrganizationIdAndRole(UUID organizationId, EnumUserRole role);
    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findByOrganizationIdAndRoleAndEmailContainingIgnoreCase(UUID orgId, EnumUserRole role, String email);
    List<UserEntity> findByOrganizationIdAndRoleAndUsernameContainingIgnoreCase(UUID orgId, EnumUserRole role, String username);

}