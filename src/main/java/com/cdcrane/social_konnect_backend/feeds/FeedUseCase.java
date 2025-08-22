package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.posts.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedUseCase {

    Page<Post> getCurrentUserFollowingFeedMostRecent(Pageable pageable);
}
