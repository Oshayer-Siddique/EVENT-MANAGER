package com.oshayer.event_manager.users.controller;

import com.oshayer.event_manager.users.dto.UpdateUserRequest;
import com.oshayer.event_manager.users.dto.OrgUserCreateRequest;
import com.oshayer.event_manager.users.dto.UserResponse;
import com.oshayer.event_manager.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PostMapping("/org")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<UserResponse> createOrgUser(@RequestBody OrgUserCreateRequest request) {
        return ResponseEntity.ok(userService.orgAdminCreateUser(request));
    }
}
