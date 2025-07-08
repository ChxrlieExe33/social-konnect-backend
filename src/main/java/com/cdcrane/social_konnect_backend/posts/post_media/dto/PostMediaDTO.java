package com.cdcrane.social_konnect_backend.posts.post_media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostMediaDTO(@JsonProperty("media_url") String mediaUrl, @JsonProperty("media_type") String mediaType) {
}
