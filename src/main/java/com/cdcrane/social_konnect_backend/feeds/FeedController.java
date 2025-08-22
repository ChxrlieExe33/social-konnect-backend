package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedUseCase feedUseCase;
    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;

    public FeedController(FeedService feedService, PostRepository postRepository, SecurityUtils securityUtils) {
        this.feedUseCase = feedService;
        this.postRepository = postRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/following")
    public ResponseEntity<Page<PostDTOWithLiked>> getCurrentUserFollowingFeedMostRecent(Pageable pageable) {

        ApplicationUser me = securityUtils.getCurrentAuth();

        Page<Post> posts = feedUseCase.getCurrentUserFollowingFeedMostRecent(pageable);

        // Extract IDs
        List<UUID> postIds = posts.getContent().stream()
                .map(Post::getId)
                .toList();

        // Get like status of these posts
        List<PostLikeStatusDTO> likeStatuses = postRepository.findLikeStatusByPostIds(postIds, me.getId());

        Map<UUID, Boolean> likeStatusMap = likeStatuses.stream()
                .collect(Collectors.toMap(
                        PostLikeStatusDTO::postId,
                        PostLikeStatusDTO::liked
                ));

        Page<PostDTOWithLiked> response = posts.map(post -> {
            boolean liked = likeStatusMap.getOrDefault(post.getId(), false);
            return new PostDTOWithLiked(post, liked);
        });

        return ResponseEntity.ok(response);

    }

}
