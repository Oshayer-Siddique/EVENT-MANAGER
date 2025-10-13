package com.oshayer.event_manager.artists.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistCreateRequest {
    @NotBlank
    private String name;
    private String description;
    @Email
    private String email;
    private String mobile;
    private String address;
    private String facebookLink;
    private String instagramLink;
    private String youtubeLink;
    private String websiteLink;
    private String imageUrl;
}
