package com.oshayer.event_manager.users.service;

import com.oshayer.event_manager.users.dto.UpdateUserRequest;
import com.oshayer.event_manager.users.dto.OrgUserCreateRequest;
import com.oshayer.event_manager.users.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUserProfile();
    UserResponse updateProfile(UpdateUserRequest request);
    UserResponse orgAdminCreateUser(OrgUserCreateRequest request);
}
