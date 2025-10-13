package com.oshayer.event_manager.sponsors.service.impl;

import com.oshayer.event_manager.sponsors.dto.*;
import com.oshayer.event_manager.sponsors.entity.SponsorEntity;
import com.oshayer.event_manager.sponsors.repository.SponsorRepository;
import com.oshayer.event_manager.sponsors.service.SponsorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SponsorServiceImpl implements SponsorService {

    private final SponsorRepository repository;

    public SponsorServiceImpl(SponsorRepository repository) {
        this.repository = repository;
    }

    @Override
    public SponsorResponse create(SponsorCreateRequest request) {
        SponsorEntity entity = SponsorEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .category(request.getCategory())
                .facebookLink(request.getFacebookLink())
                .instagramLink(request.getInstagramLink())
                .youtubeLink(request.getYoutubeLink())
                .websiteLink(request.getWebsiteLink())
                .imageUrl(request.getImageUrl())
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    public List<SponsorResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SponsorResponse getById(UUID id) {
        SponsorEntity e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sponsor not found with id: " + id));
        return toResponse(e);
    }

    @Override
    public SponsorResponse update(UUID id, SponsorUpdateRequest request) {
        SponsorEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sponsor not found with id: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getMobile() != null) existing.setMobile(request.getMobile());
        if (request.getAddress() != null) existing.setAddress(request.getAddress());
        if (request.getCategory() != null) existing.setCategory(request.getCategory());
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
            throw new RuntimeException("Sponsor not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private SponsorResponse toResponse(SponsorEntity e) {
        return SponsorResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .email(e.getEmail())
                .mobile(e.getMobile())
                .address(e.getAddress())
                .category(e.getCategory())
                .facebookLink(e.getFacebookLink())
                .instagramLink(e.getInstagramLink())
                .youtubeLink(e.getYoutubeLink())
                .websiteLink(e.getWebsiteLink())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
