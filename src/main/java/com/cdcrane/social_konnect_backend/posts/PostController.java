package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import com.cdcrane.social_konnect_backend.posts.dto.UpdatePostCaptionDTO;
import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    public PostController(PostUseCase postUseCase) {
        this.postUseCase = postUseCase;
    }

    @GetMapping("/hidden_hello")
    public ResponseEntity<String> sayHiddenHello(){
        return ResponseEntity.ok("Hello World, this is a hidden endpoint \nWelcome to Social Konnect: "
                                + SecurityContextHolder.getContext().getAuthentication().getName() + "\nWith roles: "
                                + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }


    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(Pageable pageable){

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

    // Have to use @ModelAttribute instead of @RequestBody to allow form-data
    // instead of raw JSON, since it contains files and content.
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@ModelAttribute CreatePostDTO createPostDTO) {

        Post savedPost = postUseCase.savePost(createPostDTO);

        // If just text post, return empty list.
        if(savedPost.getPostMedia() == null){

            // Return media as empty list
            return ResponseEntity.status(HttpStatus.CREATED).body(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                    List.of(), savedPost.getUser().getUsername(), savedPost.getPostedAt(), null));

        }

        // Otherwise return post with media.
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                savedPost.getPostMedia().stream()
                        .map(m -> new PostMediaDTO(m.getMediaUrl(), m.getMediaType())).toList(),
                savedPost.getUser().getUsername(), savedPost.getPostedAt(), null));

    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PostDTO>> getPostsByUsername(@PathVariable String username, Pageable pageable){

        Page<Post> posts = postUseCase.getPostsByUsername(username, pageable);

        // Map each Post in the Page to PostDTO
        var response = posts.map(this::convertPostToPostDTO);

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId){

        postUseCase.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> updatePostCaption(@PathVariable UUID postId, @RequestBody UpdatePostCaptionDTO caption){

        Post post = postUseCase.updatePostCaption(postId, caption.caption());

        PostDTO response = convertPostToPostDTO(post);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/metadata/{postId}")
    public ResponseEntity<PostMetadataDTO> getPostMetadataByPostId(@PathVariable UUID postId){

        PostMetadataDTO postMetadata = postUseCase.getPostMetadataByPostId(postId);

        return ResponseEntity.ok(postMetadata);

    }


    // ---------------------------- HELPER METHODS ----------------------------

    private PostDTO convertPostToPostDTO(Post post){

        return new PostDTO(post.getId(), post.getCaption(),
                post.getPostMedia().stream()
                        .map(media -> new PostMediaDTO(media.getMediaUrl(), media.getMediaType())).collect(Collectors.toList()),
                post.getUser().getUsername(), post.getPostedAt(), post.getUser().getProfilePictureUrl());
    }

}
