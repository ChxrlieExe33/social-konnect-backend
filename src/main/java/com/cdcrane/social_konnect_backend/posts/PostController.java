package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTO;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<PostDTO>> getAllPosts(){

        List<Post> posts = postUseCase.getAllPosts();

        List<PostDTO> response = convertPostListToPostDTOList(posts);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostDTO createPostDTO){

        List<PostMedia> media = createPostDTO.media().stream()
                .map(mediaDTO -> PostMedia.builder().mediaUrl(mediaDTO.mediaUrl()).mediaType(mediaDTO.mediaType()).build())
                .toList();

        Post post = Post.builder().caption(createPostDTO.caption()).postMedia(media).build();

        Post savedPost = postUseCase.savePost(post);

        return ResponseEntity.ok(new PostDTO(savedPost.getId(), savedPost.getCaption(),
                savedPost.getPostMedia().stream().map(m -> new PostMediaDTO(m.getMediaUrl(), m.getMediaType())).toList(),
                savedPost.getUser().getUsername(), savedPost.getPostedAt()));

    }

    @GetMapping("/{username}")
    public ResponseEntity<List<PostDTO>> getPostsByUsername(@PathVariable String username){

        List<Post> posts = postUseCase.getPostsByUsername(username);

        List<PostDTO> response = convertPostListToPostDTOList(posts);

        return ResponseEntity.ok(response);

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
