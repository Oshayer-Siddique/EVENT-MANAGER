package com.oshayer.event_manager.business_organizations.service.impl;

import com.oshayer.event_manager.business_organizations.dto.*;
import com.oshayer.event_manager.business_organizations.entity.BusinessOrganizationEntity;
import com.oshayer.event_manager.business_organizations.repository.BusinessOrganizationRepository;
import com.oshayer.event_manager.business_organizations.service.BusinessOrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BusinessOrganizationServiceImpl implements BusinessOrganizationService {

    private final BusinessOrganizationRepository repository;

    public BusinessOrganizationServiceImpl(BusinessOrganizationRepository repository) {
        this.repository = repository;
    }

    @Override
    public BusinessOrganizationResponse create(BusinessOrganizationCreateRequest request) {
        BusinessOrganizationEntity entity = BusinessOrganizationEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .facebookLink(request.getFacebookLink())
                .youtubeLink(request.getYoutubeLink())
                .websiteLink(request.getWebsiteLink())
                .imageUrl(request.getImageUrl())
                .build();

        BusinessOrganizationEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    @Override
    public List<BusinessOrganizationResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BusinessOrganizationResponse getById(UUID id) {
        BusinessOrganizationEntity e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business Organization not found with id: " + id));
        return toResponse(e);
    }

    @Override
    public BusinessOrganizationResponse update(UUID id, BusinessOrganizationUpdateRequest request) {
        BusinessOrganizationEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business Organization not found with id: " + id));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getMobile() != null) existing.setMobile(request.getMobile());
        if (request.getAddress() != null) existing.setAddress(request.getAddress());
        if (request.getFacebookLink() != null) existing.setFacebookLink(request.getFacebookLink());
        if (request.getYoutubeLink() != null) existing.setYoutubeLink(request.getYoutubeLink());
        if (request.getWebsiteLink() != null) existing.setWebsiteLink(request.getWebsiteLink());
        if (request.getImageUrl() != null) existing.setImageUrl(request.getImageUrl());

        BusinessOrganizationEntity updated = repository.save(existing);
        return toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Business Organization not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private BusinessOrganizationResponse toResponse(BusinessOrganizationEntity e) {
        return BusinessOrganizationResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .email(e.getEmail())
                .mobile(e.getMobile())
                .address(e.getAddress())
                .facebookLink(e.getFacebookLink())
                .youtubeLink(e.getYoutubeLink())
                .websiteLink(e.getWebsiteLink())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
