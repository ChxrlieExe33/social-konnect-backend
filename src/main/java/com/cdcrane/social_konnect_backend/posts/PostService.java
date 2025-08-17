package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService implements PostUseCase {

    private final PostRepository postRepo;
    private final SecurityUtils securityUtils;
    private final FileHandler fileHandler;

    @Autowired
    public PostService(PostRepository postRepo, SecurityUtils securityUtils, FileHandler fileHandler) {
        this.postRepo = postRepo;
        this.securityUtils = securityUtils;
        this.fileHandler = fileHandler;
    }

    // -------------------------------- Retrieve data --------------------------------

    /**
     * Get all posts, paginated and ordered by the creation date, newest first.
     * @param pageable Pageable data from request.
     * @return A page of posts.
     */
    @Override
    public Page<Post> getAllPosts(Pageable pageable) {

        Page<Post> posts = this.postRepo.getPostsOrderByPostedAt(pageable);

        if (posts.isEmpty()) {
            throw new ResourceNotFoundException("No posts found");
        }

        return posts;

    }

    @Override
    public Post getPostById(UUID postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found."));

        return post;

    }

    /**
     * Get all posts by username, paginated and ordered by creation date, newest first.
     * @param username The username to search by.
     * @param pageable Pageable data from request.
     * @return Page of posts created by a specific user.
     */
    @Override
    @Transactional
    public Page<Post> getPostsByUsername(String username, Pageable pageable) {

        Page<Post> posts = this.postRepo.getPostsByUsernameOrderByPostedAt(username, pageable);

        if (posts.isEmpty()) {
            throw new ResourceNotFoundException("No posts found for user with username " + username);
        }

        return posts;

    }

    /**
     * Retrieve a Page of PostDTO with the addition of a check if the user sending the request has liked these posts.
     * @param pageable For pagination
     * @return A Page of PostDTOWithLiked containing post-data and the liked boolean.
     */
    @Override
    public Page<PostDTOWithLiked> getPostsWithLiked(Pageable pageable) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        Page<Post> postsPage = this.postRepo.getPostsOrderByPostedAt(pageable);

        if(postsPage.isEmpty()) {
            throw new ResourceNotFoundException("No posts found");
        }

        // Extract IDs
        List<UUID> postIds = postsPage.getContent().stream()
                .map(Post::getId)
                .toList();

        // Get like status of these posts
        List<PostLikeStatusDTO> likeStatuses = postRepo.findLikeStatusByPostIds(postIds, user.getId());

        Map<UUID, Boolean> likeStatusMap = likeStatuses.stream()
                .collect(Collectors.toMap(
                        PostLikeStatusDTO::postId,
                        PostLikeStatusDTO::liked
                ));

        return postsPage.map(post -> {
            boolean liked = likeStatusMap.getOrDefault(post.getId(), false);
            return new PostDTOWithLiked(post, liked);
        });

    }

    /**
     * Retrieve a Page of PostDTO by the username of the poster who posted them,
     * with the addition of a check if the user sending the request has liked these posts.
     * @param username Whose posts we want to see.
     * @param pageable For pagination
     * @return A Page of PostDTOWithLiked containing post-data and the liked boolean.
     */
    @Override
    public Page<PostDTOWithLiked> getPostsWithLikedByUsername(String username, Pageable pageable) {

        Page<Post> postsPage = this.postRepo.getPostsByUsernameOrderByPostedAt(username, pageable);

        // Get the current user for the like check.
        ApplicationUser currentAuthUser = securityUtils.getCurrentAuth();


        if(postsPage.isEmpty()) {
            throw new ResourceNotFoundException("No posts found");
        }

        // Extract IDs
        List<UUID> postIds = postsPage.getContent().stream()
                .map(Post::getId)
                .toList();

        // Get like status of these posts
        List<PostLikeStatusDTO> likeStatuses = postRepo.findLikeStatusByPostIds(postIds, currentAuthUser.getId());

        Map<UUID, Boolean> likeStatusMap = likeStatuses.stream()
                .collect(Collectors.toMap(
                        PostLikeStatusDTO::postId,
                        PostLikeStatusDTO::liked
                ));

        return postsPage.map(post -> {
            boolean liked = likeStatusMap.getOrDefault(post.getId(), false);
            return new PostDTOWithLiked(post, liked);
        });

    }

    /**
     * Get the metadata of a post, including the like and comment count for now.
     * @param postId Target post-ID.
     * @return The metadata in DTO form.
     */
    @Override
    public PostMetadataDTO getPostMetadataByPostId(UUID postId) {

        return this.postRepo.getPostMetadataByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot get metadata."));

    }

    @Override
    public PostDTOWithLiked getPostWithLikedById(UUID postId) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found."));

        PostLikeStatusDTO likeStatus = postRepo.findLikeStatusByPostId(postId, user.getId());

        return new PostDTOWithLiked(post, likeStatus.liked());

    }

    @Override
    public int getPostCountByUserId(long userId) {

        return postRepo.countByUserId(userId);

    }

    // -------------------------------- Create data --------------------------------

    /**
     * Save a new post in the database, along with media if present.
     * @param createPostDTO A DTO containing Post information to persist.
     * @return The Post object saved.
     */
    @Override
    @Transactional
    public Post savePost(CreatePostDTO createPostDTO) {

        // Initialize an empty media list.
        List<PostMedia> media = new ArrayList<>();

        // If there are no files, set to null.
        if (createPostDTO.files() == null || createPostDTO.files().isEmpty()) {

            media = null;

        } else {
            media = fileHandler.saveFiles(createPostDTO.files());

        }

        Post post = Post.builder()
                .caption(createPostDTO.caption())
                .postMedia(media)
                .build();

        ApplicationUser user = securityUtils.getCurrentAuth();

        // Set the currently authed user as creator of Post.
        post.setUser(user);

        // Remove bad HTML tags from the caption.
        String cleanCaption = TextInputValidator.removeHtmlTagsAllowBasic(post.getCaption());

        post.setCaption(cleanCaption);

        return postRepo.save(post);

    }

    // -------------------------------- Delete data --------------------------------

    /**
     * Delete a post by its ID, only user who created Post can delete the post.
     * @param postId ID of the Post.
     */
    @Override
    @Transactional
    public void deletePost(UUID postId) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot delete post."));

        if(post.getUser().getId() == user.getId()){

            List<PostMedia> postMedia = post.getPostMedia();

            // Delete post in the database, will delete files if this works.
            postRepo.deleteById(postId);

            // If the post had media, delete the files.
            if(postMedia != null && !postMedia.isEmpty()){

                for (PostMedia pm : postMedia) {
                    fileHandler.deleteFile(pm.getFileName());
                }
            }

        } else {

            throw new ActionNotPermittedException("User " + user.getUsername() + " is not allowed to delete post with id " + postId + " as it does not belong to them. (Only the user who created the post can delete it.)");
        }

    }

    // -------------------------------- Update data --------------------------------

    /**
     * Update the caption of a Post by specifying the Post ID.
     * @param postId ID of the Post.
     * @param caption New caption to set.
     * @return The Updated Post object.
     */
    @Override
    @Transactional
    public Post updatePostCaption(UUID postId, String caption) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot update caption."));

        if (post.getUser().getId() != user.getId()) {

            throw new ActionNotPermittedException("User " + user.getUsername() + " is not allowed to update post with id " + postId + " as it does not belong to them. (Only the user who created the post can update it.)");
        }

        String cleanCaption = TextInputValidator.removeHtmlTagsAllowBasic(caption);

        post.setCaption(cleanCaption);

        return postRepo.save(post);

    }

}