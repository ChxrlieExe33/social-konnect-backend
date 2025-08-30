package com.cdcrane.social_konnect_backend.follows;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = ?1")
    int getFollowerCountByUserId(long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = ?1")
    int getFollowingCountByUserId(long userId);

    @Query("SELECT f.follower.id FROM Follow f WHERE f.followed.id = ?1")
    List<Long> getIdsOfFollowers(Long userId);

    void deleteByFollowerIdAndFollowedId(long followerId, long followedId);

    boolean existsByFollowerIdAndFollowedId(long followerId, long followedId);

    /**
     * Checks if the current user is following each of the users in the provided list
     *
     * @param currentUserId The ID of the current user
     * @param targetUserIds List of user IDs to check if they are being followed
     * @return List of DTOs containing each target user ID and a boolean indicating if they are being followed
     */
    @Query("SELECT new com.cdcrane.social_konnect_backend.follows.dto.FollowStatusDTO(u.id, " +
            "CASE WHEN f.id IS NOT NULL THEN true ELSE false END) " +
            "FROM ApplicationUser u LEFT JOIN Follow f ON f.followed.id = u.id AND f.follower.id = :currentUserId " +
            "WHERE u.id IN :targetUserIds")
    List<com.cdcrane.social_konnect_backend.follows.dto.FollowStatusDTO> checkFollowStatusForUsers(
            @Param("currentUserId") long currentUserId,
            @Param("targetUserIds") List<Long> targetUserIds);


    @Query("SELECT f.follower FROM Follow f WHERE f.followed.id = ?1 ORDER BY f.followedAt DESC")
    Page<ApplicationUser> getFollowersByUserId(long userId, Pageable pageable);

    @Query("SELECT f.followed FROM Follow f WHERE f.follower.id = ?1 ORDER BY f.followedAt DESC")
    Page<ApplicationUser> getFollowingByUserId(long userId, Pageable pageable);
}
