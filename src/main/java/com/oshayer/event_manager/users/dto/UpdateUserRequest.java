package com.oshayer.event_manager.users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String phone;
    private String fullName;
    private String imageUrl;
}