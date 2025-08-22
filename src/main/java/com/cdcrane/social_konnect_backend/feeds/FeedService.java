package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class FeedService implements FeedUseCase{

    private final SecurityUtils securityUtils;
    private final FollowingFeedRepository followingFeedRepository;
    private final PostRepository postRepository;

    public FeedService(SecurityUtils securityUtils, FollowingFeedRepository followingFeedRepository, PostRepository postRepository) {
        this.securityUtils = securityUtils;
        this.followingFeedRepository = followingFeedRepository;
        this.postRepository = postRepository;
    }

    /**
     * Get the following feed for the current user.
     * @param pageable Pagination data from the query params.
     * @return A page of objects with Post information and if the current user has liked the post.
     */
    public Page<PostDTOWithLiked> getCurrentUserFollowingFeedMostRecent(Pageable pageable) {

        ApplicationUser me = securityUtils.getCurrentAuth();

        Page<Post> followingPosts =  followingFeedRepository.getFollowingPostsByUserId(me.getId(), pageable);

        if (followingPosts.isEmpty()) {
            throw new ResourceNotFoundException("No posts found from users you are following.");
        }

        // Extract IDs
        List<UUID> postIds = followingPosts.getContent().stream()
                .map(Post::getId)
                .toList();

        // Get like status of these posts
        List<PostLikeStatusDTO> likeStatuses = postRepository.findLikeStatusByPostIds(postIds, me.getId());

        Map<UUID, Boolean> likeStatusMap = likeStatuses.stream()
                .collect(Collectors.toMap(
                        PostLikeStatusDTO::postId,
                        PostLikeStatusDTO::liked
                ));

        return followingPosts.map(post -> {
            boolean liked = likeStatusMap.getOrDefault(post.getId(), false);
            return new PostDTOWithLiked(post, liked);
        });

    }
}
