package com.oshayer.event_manager.users.service;

import com.oshayer.event_manager.users.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getCurrentUserProfile();
    UserResponse updateProfile(UpdateUserRequest request);
    UserResponse orgAdminCreateUser(OrgUserCreateRequest request);
    UserResponse createEventManager(CreateEventManagerRequest request);
    UserResponse createOperator(OperatorCreateRequest request);
    List<UserResponse> getAllEventManagersInOrg();
    List<UserResponse> getAllOperatorsInOrg();
    UserResponse createEventChecker(EventCheckerCreateRequest request);
    List<UserResponse> getAllEventCheckersInOrg();

    void deleteEventManager(UUID userId);
    void deleteOperator(UUID userId);
    void deleteEventChecker(UUID userId);

}
