package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.follows.FollowRepository;
import com.cdcrane.social_konnect_backend.posts.events.PostCreatedEvent;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
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

    public FeedGenerationEventListener(FollowRepository followRepository, EntityManager em, FollowingFeedRepository followingFeedRepository) {
        this.followRepository = followRepository;
        this.em = em;
        this.followingFeedRepository = followingFeedRepository;
    }

    /**
     * Handles the creation of Following feed items by retrieving the list of the users followers
     * and creating one for each.
     * @param event The recieved event.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // Using this, will only execute the event listener once the post has been committed, avoiding constraint violations.
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

}
