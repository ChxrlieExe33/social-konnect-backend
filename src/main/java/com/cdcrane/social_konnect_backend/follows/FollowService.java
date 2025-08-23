package com.cdcrane.social_konnect_backend.follows;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FollowService implements FollowUseCase {

    private final FollowRepository followRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Autowired
    public FollowService(FollowRepository followRepository, SecurityUtils securityUtils, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
    }

    @Override
    public int getFollowerCountByUserId(long userId) {

        return followRepository.getFollowerCountByUserId(userId);

    }

    @Override
    public int getFollowingCountByUserId(long userId) {

        return followRepository.getFollowingCountByUserId(userId);

    }

    /**
     * For checking if a specific user has followed another user.
     * @param followerId The person who is following.
     * @param followedId The person who is being followed.
     * @return The status of if the "follower" is actually following the "followed".
     */
    @Override
    public boolean existsByFollowerAndFollowed(long followerId, long followedId) {

        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);

    }

    /**
     * Follow a user as the current authenticated user.
     * @param username The username to follow.
     */
    @Override
    @Transactional
    public void followUser(String username) {

        ApplicationUser currentUser = securityUtils.getCurrentAuth();
        ApplicationUser targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found. Cannot follow user."));

        if (targetUser.getId() == currentUser.getId()) {
            throw new CannotFollowException("You cannot follow yourself.");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(currentUser.getId(), targetUser.getId())) {

            throw new CannotFollowException("Cannot follow user " + username + " as you already follow them.");
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .followed(targetUser)
                .build();

        followRepository.save(follow);

    }

    /**
     * Unfollow a user as the current authenticated user, if there is an existing follow.
     * @param username The username to unfollow.
     */
    @Override
    @Transactional
    public void unfollowUser(String username) {

        ApplicationUser currentUser = securityUtils.getCurrentAuth();
        ApplicationUser targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found. Cannot unfollow user."));

        if (!followRepository.existsByFollowerIdAndFollowedId(currentUser.getId(), targetUser.getId())) {

            throw new CannotFollowException("Cannot unfollow user " + username + " as you do not follow them.");
        }

        followRepository.deleteByFollowerIdAndFollowedId(currentUser.getId(), targetUser.getId());

    }

}
