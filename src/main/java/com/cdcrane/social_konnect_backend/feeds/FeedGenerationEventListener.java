package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.follows.FollowRepository;
import com.cdcrane.social_konnect_backend.follows.events.UserFollowedEvent;
import com.cdcrane.social_konnect_backend.follows.events.UserUnfollowedEvent;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.posts.events.PostCreatedEvent;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
@EnableAsync
public class FeedGenerationEventListener {

    private final FollowRepository followRepository;
    private final EntityManager em;
    private final FollowingFeedRepository followingFeedRepository;
    private final PostRepository postRepo;

    public FeedGenerationEventListener(FollowRepository followRepository, EntityManager em, FollowingFeedRepository followingFeedRepository, PostRepository postRepository) {
        this.followRepository = followRepository;
        this.em = em;
        this.followingFeedRepository = followingFeedRepository;
        this.postRepo = postRepository;
    }

    /**
     * Handles the creation of Following feed items by retrieving the list of the user's followers
     * and creating one for each.
     * @param event The received event.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // With this, the listener will only execute the event listener once the post has been committed, avoiding constraint violations.
    public void listenForPostCreatedAndGenerateFollowingFeed(PostCreatedEvent event) {

        List<Long> followerIds = followRepository.getIdsOfFollowers(event.user().getId());

        List<FollowingFeedItem> feedItems = followerIds.stream()
                .map(fid -> FollowingFeedItem.builder()
                        .feedOwner(em.getReference(ApplicationUser.class, fid)) // Using entityManager to only get the reference instead of the entire object, for efficiency.
                        .post(event.post())
                        .build()
                )
                .toList();

        followingFeedRepository.saveAll(feedItems);
    }

    /**
     * When a user-followed event is published, this will add the 5 recent posts of the followed user to the follower's feed.
     * Without this, the follower's feed would remain empty until the followed posts again.
     * @param event The event, which contains the IDs of the follower and followed users.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void listenForUserFollowedAndAddPostsToFollowingFeed(UserFollowedEvent event) {

        List<Post> recent5Posts = postRepo.getXRecentPostsByUsername(event.followedId(), PageRequest.of(0, 5));

        List<FollowingFeedItem> newFeedItems = recent5Posts.stream()
                .map(post -> FollowingFeedItem.builder()
                        .feedOwner(em.getReference(ApplicationUser.class, event.followerId()))
                        .post(post)
                        .build()
                )
                .toList();

        followingFeedRepository.saveAll(newFeedItems);

        log.info("Added {} posts to following feed for user {}.", newFeedItems.size(), event.followedId());
    }

    /**
     * When a user-unfollowed event is published, this will remove all posts by the unfollowed user from the unfollower's feed.
     * @param event The event, which contains the IDs of the unfollower and unfollowed users.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void listenForUserUnfollowedAndDeletePostsFromFollowingFeed(UserUnfollowedEvent event) {

        List<FollowingFeedItem> toRemove = followingFeedRepository.findByFeedOwnerIdAndFollowedUserId(event.followerId(), event.followedId());

        followingFeedRepository.deleteAll(toRemove);

        log.info("Deleted {} posts from following feed for user {}.", toRemove.size(), event.followedId());

    }

}
