package com.cdcrane.social_konnect_backend.comments.dto;

import java.util.UUID;

public record CommentCountDTO(UUID postId, int count) {
}
