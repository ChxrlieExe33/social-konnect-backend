package com.cdcrane.social_konnect_backend.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserMetadataDTO(@JsonProperty("followers") Integer followersCount,
                              @JsonProperty("following") Integer followingCount,
                              @JsonProperty("posts") Integer postsCount,
                              @JsonProperty("current_user_follows") Boolean currentUserFollows) {
}
