package com.oshayer.event_manager.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperatorCreateRequest {
    private String email;
    private String phone;
    private String fullName;
    private String password;
}

