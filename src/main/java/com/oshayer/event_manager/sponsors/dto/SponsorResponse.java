package com.oshayer.event_manager.sponsors.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorResponse {
    private UUID id;
    private String name;
    private String description;
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
