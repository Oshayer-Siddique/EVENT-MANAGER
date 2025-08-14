package com.oshayer.event_manager.organizations.repository;


import com.oshayer.event_manager.organizations.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
}
