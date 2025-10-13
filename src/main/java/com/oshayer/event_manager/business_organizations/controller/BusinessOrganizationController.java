package com.oshayer.event_manager.business_organizations.controller;

import com.oshayer.event_manager.business_organizations.dto.*;
import com.oshayer.event_manager.business_organizations.service.BusinessOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-organizations")
public class BusinessOrganizationController {

    private final BusinessOrganizationService service;

    public BusinessOrganizationController(BusinessOrganizationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BusinessOrganizationResponse> create(
            @Valid @RequestBody BusinessOrganizationCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BusinessOrganizationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessOrganizationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessOrganizationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessOrganizationUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
