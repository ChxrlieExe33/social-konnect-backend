package com.cdcrane.social_konnect_backend.posts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PostMetadataDTO(@JsonProperty("post_id") UUID postId, Long likes, Long comments) {
}
