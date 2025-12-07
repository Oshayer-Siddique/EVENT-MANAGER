package com.oshayer.event_manager.discounts.controller;

import com.oshayer.event_manager.discounts.dto.*;
import com.oshayer.event_manager.discounts.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public ResponseEntity<DiscountResponse> create(@Valid @RequestBody DiscountCreateRequest request) {
        return ResponseEntity.ok(discountService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody DiscountUpdateRequest request) {
        return ResponseEntity.ok(discountService.update(id, request));
    }

    @GetMapping
    public ResponseEntity<List<DiscountResponse>> list(@RequestParam(required = false) UUID eventId) {
        return ResponseEntity.ok(discountService.list(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(discountService.get(id));
    }

    @PostMapping("/validate")
    public ResponseEntity<DiscountValidationResponse> validate(@Valid @RequestBody DiscountValidationRequest request) {
        return ResponseEntity.ok(discountService.preview(request));
    }
}
