package com.oshayer.event_manager.sponsors.service;

import com.oshayer.event_manager.sponsors.dto.*;

import java.util.List;
import java.util.UUID;

public interface SponsorService {
    SponsorResponse create(SponsorCreateRequest request);
    List<SponsorResponse> getAll();
    SponsorResponse getById(UUID id);
    SponsorResponse update(UUID id, SponsorUpdateRequest request);
    void delete(UUID id);
}
