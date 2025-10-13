package com.oshayer.event_manager.sponsors.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCreateRequest {
    @NotBlank
    private String name;
    private String description;
    @Email
    private String email;
    private String mobile;
    private String address;
    private String category;
    private String facebookLink;
    private String instagramLink;
    private String youtubeLink;
    private String websiteLink;
    private String imageUrl;
}
