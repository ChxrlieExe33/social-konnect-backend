package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.posts.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    int countByPostId(UUID postId);

    Page<Comment> findByPostId(UUID postId, Pageable pageable);

    UUID post(Post post);
}
