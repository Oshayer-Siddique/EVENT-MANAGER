package com.oshayer.event_manager.users.repository;

import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
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

    @Query("""
        select count(u) from UserEntity u
        where (:start is null or u.createdAt >= :start)
          and (:end is null or u.createdAt <= :end)
    """)
    long countNewUsers(OffsetDateTime start, OffsetDateTime end);

    @Query("""
        select u.role as role, count(u) as count
        from UserEntity u
        where (:start is null or u.createdAt >= :start)
          and (:end is null or u.createdAt <= :end)
        group by u.role
    """)
    List<RoleCountView> countByRole(OffsetDateTime start, OffsetDateTime end);

    interface RoleCountView {
        EnumUserRole getRole();
        Long getCount();
    }

}
