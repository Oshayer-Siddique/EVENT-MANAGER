package com.oshayer.event_manager.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {
    private String email;
    private String phone;
    private String fullName; // no @NotBlank, no @NotNull
    private String imageUrl;
    private String password;
}
