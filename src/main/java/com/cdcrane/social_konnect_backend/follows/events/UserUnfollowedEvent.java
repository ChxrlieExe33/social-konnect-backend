package com.cdcrane.social_konnect_backend.follows.events;

public record UserUnfollowedEvent(long followerId, long followedId) {
}
