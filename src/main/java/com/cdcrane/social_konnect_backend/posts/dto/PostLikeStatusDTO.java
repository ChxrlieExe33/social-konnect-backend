package com.cdcrane.social_konnect_backend.posts.dto;

import java.util.UUID;

public record PostLikeStatusDTO(UUID postId, boolean liked) {
}
