package com.oshayer.event_manager.users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventManagerRequest {

    private String email;       // required
    private String phone;       // optional
    private String firstName;   // required
    private String lastName;    // required
    private String fullName;    // optional, auto-generated if null
    private String imageUrl;    // optional
    private String password;    // required
    private String username;    // required, must be unique
}
