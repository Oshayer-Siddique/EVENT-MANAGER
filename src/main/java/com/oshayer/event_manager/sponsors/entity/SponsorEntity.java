package com.oshayer.event_manager.sponsors.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "sponsors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String email;
    private String mobile;

    @Column(length = 1000)
    private String address;

    private String category;

    @Column(name = "facebook_link")
    private String facebookLink;

    @Column(name = "instagram_link")
    private String instagramLink;

    @Column(name = "youtube_link")
    private String youtubeLink;

    @Column(name = "website_link")
    private String websiteLink;

    @Column(name = "image_url")
    private String imageUrl;
}
