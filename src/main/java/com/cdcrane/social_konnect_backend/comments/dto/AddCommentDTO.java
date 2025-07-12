package com.cdcrane.social_konnect_backend.comments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCommentDTO(@NotBlank String content,
                            @NotNull @JsonProperty("post_id") UUID postId) {
}
