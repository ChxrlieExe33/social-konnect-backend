package com.cdcrane.social_konnect_backend.follows.events;

public record UserFollowedEvent(long followerId, long followedId) {
}
