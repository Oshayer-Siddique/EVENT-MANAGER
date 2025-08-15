package com.oshayer.event_manager.users.service;

import com.oshayer.event_manager.users.dto.*;

public interface UserService {
    UserResponse getCurrentUserProfile();
    UserResponse updateProfile(UpdateUserRequest request);
    UserResponse orgAdminCreateUser(OrgUserCreateRequest request);
    UserResponse createEventManager(CreateEventManagerRequest request);
    UserResponse createOperator(OperatorCreateRequest request);


}
