package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostUseCase {

    Page<Post> getAllPosts(Pageable pageable);

    Page<Post> getPostsByUsername(String username, Pageable pageable);

    Page<PostDTOWithLiked> getPostsWithLiked(Pageable pageable);

    Page<PostDTOWithLiked> getPostsWithLikedByUsername(String username, Pageable pageable);

    PostDTOWithLiked getPostWithLikedById(UUID postId);

    Post getPostById(UUID postId);

    PostMetadataDTO getPostMetadataByPostId(UUID postId);

    Post savePost(CreatePostDTO createPostDTO);

    void deletePost(UUID postId);

    Post updatePostCaption(UUID postId, String caption);

}
