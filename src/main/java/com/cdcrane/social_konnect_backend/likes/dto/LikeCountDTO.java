package com.cdcrane.social_konnect_backend.likes.dto;

import java.util.UUID;

public record LikeCountDTO(UUID postId, int likeCount) {
}
