package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class FeedService implements FeedUseCase{

    private final SecurityUtils securityUtils;
    private final FollowingFeedRepository followingFeedRepository;

    public FeedService(SecurityUtils securityUtils, FollowingFeedRepository followingFeedRepository) {
        this.securityUtils = securityUtils;
        this.followingFeedRepository = followingFeedRepository;
    }

    public Page<Post> getCurrentUserFollowingFeedMostRecent(Pageable pageable) {

        ApplicationUser me = securityUtils.getCurrentAuth();

        Page<Post> followingPosts =  followingFeedRepository.getFollowingPostsByUserId(me.getId(), pageable);

        if (followingPosts.isEmpty()) {
            throw new ResourceNotFoundException("No posts found from users you are following.");
        }

        return followingPosts;

    }
}
