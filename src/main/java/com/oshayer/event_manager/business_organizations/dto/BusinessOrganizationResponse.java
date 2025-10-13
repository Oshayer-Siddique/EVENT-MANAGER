package com.oshayer.event_manager.business_organizations.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessOrganizationResponse {
    private UUID id;
    private String name;
    private String description;
    private String email;
    private String mobile;
    private String address;
    private String facebookLink;
    private String youtubeLink;
    private String websiteLink;
    private String imageUrl;
}
