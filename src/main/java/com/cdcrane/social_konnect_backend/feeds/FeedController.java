package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedUseCase feedUseCase;

    public FeedController(FeedService feedService) {
        this.feedUseCase = feedService;
    }

    @GetMapping("/following")
    public ResponseEntity<Page<PostDTOWithLiked>> getCurrentUserFollowingFeedMostRecent(Pageable pageable) {

        Page<PostDTOWithLiked> posts = feedUseCase.getCurrentUserFollowingFeedMostRecent(pageable);

        return ResponseEntity.ok(posts);

    }

}
