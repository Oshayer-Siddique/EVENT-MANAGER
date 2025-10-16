package com.oshayer.event_manager.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventCheckerCreateRequest {
    private String username;
    private String password;

    private String email;
    private String phone;

    private String firstName;
    private String lastName;
    private String fullName;   // optional; fallback = firstName + " " + lastName
    private String imageUrl;   // optional
}
