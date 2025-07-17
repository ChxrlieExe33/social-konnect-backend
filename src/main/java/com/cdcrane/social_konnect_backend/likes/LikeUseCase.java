package com.cdcrane.social_konnect_backend.likes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface LikeUseCase {

    Like likePost(UUID postId);

    void unlikePost(UUID postId);

    int getLikeCountByPostId(UUID postId);

    Page<String> getUsernamesWhoLikePost(UUID postId, Pageable pageable);

}
