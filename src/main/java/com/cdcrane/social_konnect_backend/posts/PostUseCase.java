package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostUseCase {

    Page<Post> getAllPosts(Pageable pageable);

    Page<Post> getPostsByUsername(String username, Pageable pageable);

    Post getPostById(UUID postId);

    Post savePost(CreatePostDTO createPostDTO);

    void deletePost(UUID postId);

    Post updatePostCaption(UUID postId, String caption);
}
