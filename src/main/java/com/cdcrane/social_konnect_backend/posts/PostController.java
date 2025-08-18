package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.posts.dto.*;
import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostUseCase postUseCase;
    private final SecurityUtils securityUtils;

    @Autowired
    public PostController(PostUseCase postUseCase, SecurityUtils securityUtils) {
        this.postUseCase = postUseCase;
        this.securityUtils = securityUtils;
    }

    // -------------------------------- GET mappings --------------------------------

    @GetMapping("/hidden_hello")
    public ResponseEntity<String> sayHiddenHello(){
        return ResponseEntity.ok("Hello World, this is a hidden endpoint \nWelcome to Social Konnect: "
                                + SecurityContextHolder.getContext().getAuthentication().getName() + "\nWith roles: "
                                + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }


    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(@PageableDefault(size = 10) Pageable pageable){

        Page<Post> posts = postUseCase.getAllPosts(pageable);

        // Map each Post in the Page to PostDTO
        var response = posts.map(this::convertPostToPostDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable UUID postId){

        Post post = postUseCase.getPostById(postId);

        PostDTO response = convertPostToPostDTO(post);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/metadata/{postId}")
    public ResponseEntity<PostMetadataDTO> getPostMetadataByPostId(@PathVariable UUID postId){

        PostMetadataDTO postMetadata = postUseCase.getPostMetadataByPostId(postId);

        return ResponseEntity.ok(postMetadata);

    }

    @GetMapping("/all-with-liked-check")
    public ResponseEntity<Page<PostDTOWithLiked>> getPostsWithLiked(@PageableDefault(size = 10) Pageable pageable){

        Page<PostDTOWithLiked> posts = postUseCase.getPostsWithLiked(pageable);

        return ResponseEntity.ok(posts);

    }

    @GetMapping("/by-username-with-liked-check/{username}")
    public ResponseEntity<Page<PostDTOWithLiked>> getPostsWithLikedByUsername(@PathVariable String username, @PageableDefault(size = 10) Pageable pageable){

        Page<PostDTOWithLiked> posts = postUseCase.getPostsWithLikedByUsername(username, pageable);

        return ResponseEntity.ok(posts);

    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PostDTO>> getPostsByUsername(@PathVariable String username, Pageable pageable){

        Page<Post> posts = postUseCase.getPostsByUsername(username, pageable);

        // Map each Post in the Page to PostDTO
        var response = posts.map(this::convertPostToPostDTO);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/with-liked/{postId}")
    public ResponseEntity<PostDTOWithLiked> getPostLikeStatus(@PathVariable UUID postId){

        PostDTOWithLiked response = postUseCase.getPostWithLikedById(postId);

        return ResponseEntity.ok(response);

    }

    // -------------------------------- POST mappings --------------------------------

    // Have to use @ModelAttribute instead of @RequestBody to allow form-data
    // instead of raw JSON, since it contains files and content.
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@ModelAttribute CreatePostDTO createPostDTO) {

        Post savedPost = postUseCase.savePost(createPostDTO);

        ApplicationUser auth = securityUtils.getCurrentAuth();

        // If just text post, return empty list.
        if(savedPost.getPostMedia() == null){

            // Return media as empty list
            return ResponseEntity.status(HttpStatus.CREATED).body(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                    List.of(), savedPost.getUser().getUsername(), savedPost.getPostedAt(), auth.getProfilePictureUrl()));

        }

        // Otherwise return post with media.
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                savedPost.getPostMedia().stream()
                        .map(m -> new PostMediaDTO(m.getMediaUrl(), m.getMediaType())).toList(),
                savedPost.getUser().getUsername(), savedPost.getPostedAt(), auth.getProfilePictureUrl()));

    }


    // -------------------------------- DELETE mappings --------------------------------

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId){

        postUseCase.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    // -------------------------------- PUT mappings --------------------------------

    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> updatePostCaption(@PathVariable UUID postId, @RequestBody UpdatePostCaptionDTO caption){

        Post post = postUseCase.updatePostCaption(postId, caption.caption());

        PostDTO response = convertPostToPostDTO(post);

        return ResponseEntity.ok(response);

    }


    // -------------------------------- HELPER METHODS --------------------------------

    private PostDTO convertPostToPostDTO(Post post){

        return new PostDTO(post.getId(), post.getCaption(),
                post.getPostMedia().stream()
                        .map(media -> new PostMediaDTO(media.getMediaUrl(), media.getMediaType())).collect(Collectors.toList()),
                post.getUser().getUsername(), post.getPostedAt(), post.getUser().getProfilePictureUrl());
    }

}
