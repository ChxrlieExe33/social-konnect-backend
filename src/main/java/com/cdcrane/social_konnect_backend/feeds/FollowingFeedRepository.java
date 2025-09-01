package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.posts.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowingFeedRepository extends JpaRepository<FollowingFeedItem, UUID> {

    @Query("SELECT p FROM FollowingFeedItem ffi JOIN ffi.post p WHERE ffi.feedOwner.id = ?1 ORDER BY p.postedAt DESC")
    Page<Post> getFollowingPostsByUserId(long userId, Pageable pageable);

    @Query("SELECT i FROM FollowingFeedItem i WHERE i.feedOwner.id = ?1 AND i.post.user.id = ?2")
    List<FollowingFeedItem> findByFeedOwnerIdAndFollowedUserId(long feedOwnerId, long followedUserId);
}
