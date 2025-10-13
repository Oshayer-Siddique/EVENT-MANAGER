package com.oshayer.event_manager.artists.service.impl;

import com.oshayer.event_manager.artists.dto.*;
import com.oshayer.event_manager.artists.entity.ArtistEntity;
import com.oshayer.event_manager.artists.repository.ArtistRepository;
import com.oshayer.event_manager.artists.service.ArtistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository repository;

    public ArtistServiceImpl(ArtistRepository repository) {
        this.repository = repository;
    }

    @Override
    public ArtistResponse create(ArtistCreateRequest request) {
        ArtistEntity entity = ArtistEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .facebookLink(request.getFacebookLink())
                .instagramLink(request.getInstagramLink())
                .youtubeLink(request.getYoutubeLink())
                .websiteLink(request.getWebsiteLink())
                .imageUrl(request.getImageUrl())
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    public List<ArtistResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ArtistResponse getById(UUID id) {
        ArtistEntity e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + id));
        return toResponse(e);
    }

    @Override
    public ArtistResponse update(UUID id, ArtistUpdateRequest request) {
        ArtistEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getMobile() != null) existing.setMobile(request.getMobile());
        if (request.getAddress() != null) existing.setAddress(request.getAddress());
        if (request.getFacebookLink() != null) existing.setFacebookLink(request.getFacebookLink());
        if (request.getInstagramLink() != null) existing.setInstagramLink(request.getInstagramLink());
        if (request.getYoutubeLink() != null) existing.setYoutubeLink(request.getYoutubeLink());
        if (request.getWebsiteLink() != null) existing.setWebsiteLink(request.getWebsiteLink());
        if (request.getImageUrl() != null) existing.setImageUrl(request.getImageUrl());

        return toResponse(repository.save(existing));
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Artist not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private ArtistResponse toResponse(ArtistEntity e) {
        return ArtistResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .email(e.getEmail())
                .mobile(e.getMobile())
                .address(e.getAddress())
                .facebookLink(e.getFacebookLink())
                .instagramLink(e.getInstagramLink())
                .youtubeLink(e.getYoutubeLink())
                .websiteLink(e.getWebsiteLink())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
