package com.oshayer.event_manager.users.controller;

import com.oshayer.event_manager.users.dto.*;
import com.oshayer.event_manager.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PostMapping("/org")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<UserResponse> createOrgUser(@RequestBody OrgUserCreateRequest request) {
        return ResponseEntity.ok(userService.orgAdminCreateUser(request));
    }

    @PostMapping("/event-manager")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<UserResponse> createEventManager(@RequestBody CreateEventManagerRequest request) {
        return ResponseEntity.ok(userService.createEventManager(request));
    }

    @PostMapping("/operator")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<UserResponse> createOperator(@RequestBody OperatorCreateRequest request) {
        return ResponseEntity.ok(userService.createOperator(request));
    }

    // ---------- NEW: Event Checker endpoints ----------
    @PostMapping("/event-checker")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<UserResponse> createEventChecker(@RequestBody EventCheckerCreateRequest request) {
        return ResponseEntity.ok(userService.createEventChecker(request));
    }

    @GetMapping("/event-checkers")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllEventCheckers() {
        return ResponseEntity.ok(userService.getAllEventCheckersInOrg());
    }
    // ---------------------------------------------------

    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @GetMapping("/event-managers")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllEventManagers() {
        return ResponseEntity.ok(userService.getAllEventManagersInOrg());
    }

    @GetMapping("/operators")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllOperators() {
        return ResponseEntity.ok(userService.getAllOperatorsInOrg());
    }
}
