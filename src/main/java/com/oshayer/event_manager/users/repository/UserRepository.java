package com.oshayer.event_manager.users.repository;

import com.oshayer.event_manager.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmailVerificationToken(String token);
    Optional<UserEntity> findByResetPasswordToken(String token);


}