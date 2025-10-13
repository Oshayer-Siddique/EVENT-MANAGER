package com.oshayer.event_manager.business_organizations.repository;

import com.oshayer.event_manager.business_organizations.entity.BusinessOrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessOrganizationRepository extends JpaRepository<BusinessOrganizationEntity, UUID> {
    Optional<BusinessOrganizationEntity> findByName(String name);
}
