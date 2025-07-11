package com.cdcrane.social_konnect_backend.posts;

import java.util.List;
import java.util.UUID;

public interface PostUseCase {

    List<Post> getAllPosts();

    List<Post> getPostsByUsername(String username);

    Post savePost(Post post);

    void deletePost(UUID postId);

    Post updatePostCaption(UUID postId, String caption);
}
