package com.cdcrane.social_konnect_backend.likes;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.dto.UsernameAndPfpDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


    /**
     * Add a like to a post as the currently authenticated user.
     * @param postId The ID of the post to add the like to.
     * @return The Like object after creation.
     */
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

    /**
     * Remove a like if it exists from the indicated post as the currently authenticated user.
     * @param postId The post-ID to remove the like from.
     */
    @Override
    public void unlikePost(UUID postId) {

        ApplicationUser auth = securityUtils.getCurrentAuth();

        Like like = likeRepository.findByPostIdAndUserId(postId, auth.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Like with post id " + postId + " and user id " + auth.getId() + " not found, cannot unlike post."));

        likeRepository.deleteById(like.getId());

    }

    /**
     * Get the number of likes of a specific post.
     * @param postId The ID of the post in question.
     * @return The number of likes.
     */
    @Override
    public int getLikeCountByPostId(UUID postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot get like count."));

        return likeRepository.countByPostId(postId);

    }

    /**
     * Get a page of usernames who have liked a specific post.
     * @param postId The ID of the post in question.
     * @param pageable Pagination data from the query params.
     * @return The Page of users who have liked the post.
     */
    @Override
    public Page<UsernameAndPfpDTO> getUsernamesWhoLikePost(UUID postId, Pageable pageable) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot get like count."));

        Page<Like> likes = likeRepository.findByPostId(postId, pageable);

        if (likes.isEmpty()) {
            throw new ResourceNotFoundException("No likes found for post with id " + postId);
        }

        return likes.map(like -> new UsernameAndPfpDTO(like.getUser().getUsername(), like.getUser().getProfilePictureUrl()));

    }
}
