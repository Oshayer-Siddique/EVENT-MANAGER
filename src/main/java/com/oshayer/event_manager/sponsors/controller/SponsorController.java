package com.oshayer.event_manager.sponsors.controller;

import com.oshayer.event_manager.sponsors.dto.*;
import com.oshayer.event_manager.sponsors.service.SponsorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sponsors")
public class SponsorController {

    private final SponsorService service;

    public SponsorController(SponsorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SponsorResponse> create(@Valid @RequestBody SponsorCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SponsorResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SponsorResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SponsorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SponsorUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
