package com.cdcrane.social_konnect_backend.posts.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePostCaptionDTO (@NotNull String caption){
}
