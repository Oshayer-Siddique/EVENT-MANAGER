package com.oshayer.event_manager.business_organizations.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessOrganizationUpdateRequest {
    private String name;
    private String description;
    @Email
    private String email;
    private String mobile;
    private String address;
    private String facebookLink;
    private String youtubeLink;
    private String websiteLink;
    private String imageUrl;
}
