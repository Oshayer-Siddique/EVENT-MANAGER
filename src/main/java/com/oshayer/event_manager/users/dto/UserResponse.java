package com.oshayer.event_manager.users.dto;

import com.oshayer.event_manager.users.entity.EnumUserRole;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private String imageUrl;
    private EnumUserRole role;
    private boolean emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID organizationId;
}