package com.oshayer.event_manager.artists.service;

import com.oshayer.event_manager.artists.dto.*;

import java.util.List;
import java.util.UUID;

public interface ArtistService {
    ArtistResponse create(ArtistCreateRequest request);
    List<ArtistResponse> getAll();
    ArtistResponse getById(UUID id);
    ArtistResponse update(UUID id, ArtistUpdateRequest request);
    void delete(UUID id);
}

