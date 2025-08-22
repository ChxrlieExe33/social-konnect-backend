package com.cdcrane.social_konnect_backend.posts.events;

import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;

public record PostCreatedEvent (Post post, ApplicationUser user) {
}
