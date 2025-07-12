package com.cdcrane.social_konnect_backend.comments.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDTO (UUID id, String content, Instant createdAt){
}
