package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTO;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostUseCase postUseCase;
    private final FileHandler fileHandler;

    @Autowired
    public PostController(PostUseCase postUseCase, FileHandler fileHandler) {
        this.postUseCase = postUseCase;
        this.fileHandler = fileHandler;
    }

    @GetMapping("/hidden_hello")
    public ResponseEntity<String> sayHiddenHello(){
        return ResponseEntity.ok("Hello World, this is a hidden endpoint \nWelcome to Social Konnect: "
                                + SecurityContextHolder.getContext().getAuthentication().getName() + "\nWith roles: "
                                + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }


    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(){

        List<Post> posts = postUseCase.getAllPosts();

        List<PostDTO> response = convertPostListToPostDTOList(posts);

        return ResponseEntity.ok(response);
    }

    // Have to use @ModelAttribute instead of @RequestBody to allow form-data
    // instead of raw JSON, since it contains files and content.
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@ModelAttribute CreatePostDTO createPostDTO) {

        List<PostMedia> media = new ArrayList<>();

        if (createPostDTO.files() == null || createPostDTO.files().isEmpty()) {

            media = null;

        } else {
            media = fileHandler.saveFiles(createPostDTO.files());

        }

        Post post = Post.builder().caption(createPostDTO.caption()).postMedia(media).build();

        Post savedPost = postUseCase.savePost(post);

        // If just text post
        if(savedPost.getPostMedia() == null){

            // Return media as empty list
            return ResponseEntity.ok(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                    List.of(), savedPost.getUser().getUsername(), savedPost.getPostedAt()));

        }

        // Otherwise return media.
        return ResponseEntity.ok(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                savedPost.getPostMedia().stream()
                        .map(m -> new PostMediaDTO(m.getMediaUrl(), m.getMediaType())).toList(),
                savedPost.getUser().getUsername(), savedPost.getPostedAt()));

    }

    @GetMapping("/{username}")
    public ResponseEntity<List<PostDTO>> getPostsByUsername(@PathVariable String username){

        List<Post> posts = postUseCase.getPostsByUsername(username);

        List<PostDTO> response = convertPostListToPostDTOList(posts);

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId){

        postUseCase.deletePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    private List<PostDTO> convertPostListToPostDTOList(List<Post> posts){

        return posts.stream()
                .map(post -> new PostDTO(post.getId(), post.getCaption(),
                        post.getPostMedia().stream()
                                .map(media -> new PostMediaDTO(media.getMediaUrl(), media.getMediaType())).collect(Collectors.toList()),
                        post.getUser().getUsername(), post.getPostedAt()))
                .toList();

    }

}
