package com.cdcrane.social_konnect_backend.comments.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDataDTO(UUID commentId, String content, Instant createdAt, String username, String profilePictureUrl) {
}
