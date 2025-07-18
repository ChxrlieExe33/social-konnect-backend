package com.cdcrane.social_konnect_backend.likes;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class LikeService implements LikeUseCase {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository, SecurityUtils securityUtils) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.securityUtils = securityUtils;
    }


    @Override
    public Like likePost(UUID postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot like post."));

        ApplicationUser auth = securityUtils.getCurrentAuth();

        // Make sure the user hasn't already liked the post.
        if (likeRepository.existsByPostIdAndUserId(post.getId(), auth.getId())) {

            throw new ActionNotPermittedException("User " + auth.getUsername() + " is not allowed to like post with id more than once. (Only the user who has not liked the post can like it.)");
        }

        Like like = Like.builder()
                .user(auth)
                .post(post)
                .build();

        return likeRepository.save(like);

    }

    @Override
    public void unlikePost(UUID postId) {

        ApplicationUser auth = securityUtils.getCurrentAuth();

        Like like = likeRepository.findByPostIdAndUserId(postId, auth.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Like with post id " + postId + " and user id " + auth.getId() + " not found, cannot unlike post."));

        likeRepository.deleteById(like.getId());

    }

    @Override
    public int getLikeCountByPostId(UUID postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot get like count."));

        return likeRepository.countByPostId(postId);

    }

    @Override
    public Page<String> getUsernamesWhoLikePost(UUID postId, Pageable pageable) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot get like count."));

        return likeRepository.getUsernamesWhoLikePost(postId, pageable);

    }
}
