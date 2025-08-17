package com.cdcrane.social_konnect_backend.follows;

public interface FollowUseCase {

    int getFollowerCountByUserId(long userId);

    int getFollowingCountByUserId(long userId);

    boolean existsByFollowerAndFollowed(long followerId, long followedId);

    void followUser(String username);

    void unfollowUser(String username);

}
