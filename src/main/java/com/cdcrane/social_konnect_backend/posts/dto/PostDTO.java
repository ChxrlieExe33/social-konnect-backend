package com.cdcrane.social_konnect_backend.posts.dto;

import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDTO(UUID postId, String caption, List<PostMediaDTO> media, String username, Instant createdAt) {
}
