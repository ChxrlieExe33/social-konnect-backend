package com.cdcrane.social_konnect_backend.follows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = ?1")
    int getFollowerCountByUserId(long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = ?1")
    int getFollowingCountByUserId(long userId);

    void deleteByFollowerIdAndFollowedId(long followerId, long followedId);

    boolean existsByFollowerIdAndFollowedId(long followerId, long followedId);

}
