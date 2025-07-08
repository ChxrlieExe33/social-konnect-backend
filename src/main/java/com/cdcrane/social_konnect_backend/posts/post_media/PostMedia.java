package com.cdcrane.social_konnect_backend.posts.post_media;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "media_id")
    private UUID id;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "media_type")
    private String mediaType;

}
