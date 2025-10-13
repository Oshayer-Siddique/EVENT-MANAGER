package com.oshayer.event_manager.business_organizations.service;

import com.oshayer.event_manager.business_organizations.dto.BusinessOrganizationCreateRequest;
import com.oshayer.event_manager.business_organizations.dto.BusinessOrganizationResponse;
import com.oshayer.event_manager.business_organizations.dto.BusinessOrganizationUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface BusinessOrganizationService {
    BusinessOrganizationResponse create(BusinessOrganizationCreateRequest request);
    List<BusinessOrganizationResponse> getAll();
    BusinessOrganizationResponse getById(UUID id);
    BusinessOrganizationResponse update(UUID id, BusinessOrganizationUpdateRequest request);
    void delete(UUID id);
}
