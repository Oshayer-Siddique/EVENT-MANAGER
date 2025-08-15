package com.oshayer.event_manager.users.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventManagerRequest {
    private String email;
    private String phone;
    private String fullName;
    private String imageUrl;
    private String password;
    private UUID organizationId; // optional if same org as ORG_ADMIN
}
