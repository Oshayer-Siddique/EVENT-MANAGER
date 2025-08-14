package com.oshayer.event_manager.users.dto;

import com.oshayer.event_manager.users.entity.EnumUserRole;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgUserCreateRequest {
    private String email;
    private String phone;
    private String fullName;
    private String imageUrl;
    private String password;
    private EnumUserRole role; // ROLE_EVENT_MANAGER or ROLE_OPERATOR
    private UUID organizationId;
}