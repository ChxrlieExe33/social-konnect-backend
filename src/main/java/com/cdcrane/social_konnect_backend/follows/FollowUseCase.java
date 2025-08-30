package com.cdcrane.social_konnect_backend.follows;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowUseCase {

    int getFollowerCountByUserId(long userId);

    int getFollowingCountByUserId(long userId);

    boolean existsByFollowerAndFollowed(long followerId, long followedId);

    void followUser(String username);

    void unfollowUser(String username);

    Page<ApplicationUser> getMyFollowers(Pageable pageable);

    Page<ApplicationUser> getMyFollowing(Pageable pageable);

}
