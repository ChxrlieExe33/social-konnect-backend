package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.comments.Comment;
import com.cdcrane.social_konnect_backend.comments.CommentRepository;
import com.cdcrane.social_konnect_backend.likes.Like;
import com.cdcrane.social_konnect_backend.likes.LikeRepository;
import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository underTest;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private LikeRepository likeRepo;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void shouldGetPostsOrderedByPostedAt(){

        // Given
        Post post1 = new Post();
        post1.setCaption("Post 1");
        underTest.save(post1);

        Post post2 = new Post();
        post2.setCaption("Post 2");
        underTest.save(post2);

        Pageable pageable = Pageable.unpaged();

        // When
        Page<Post> results = underTest.getPostsOrderByPostedAt(pageable);

        // Then
        assertEquals(2, results.getNumberOfElements());

        List<Post> posts = results.toList();
        assertEquals("Post 2", posts.get(0).getCaption()); // This one was created last, so it should be first
        assertEquals("Post 1", posts.get(1).getCaption()); // This one was created first, so it should be after

    }

    @Test
    void shouldNotGetPostsOrderedByPostedAt(){

        // Given no posts
        Pageable pageable = Pageable.unpaged();

        // When
        Page<Post> results = underTest.getPostsOrderByPostedAt(pageable);

        // Then
        assertEquals(0, results.getNumberOfElements());

    }

    @Test
    void shouldGetPostsByUsernameOrderedByPostedAt() {

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").build();

        Instant earlierTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant laterTime = Instant.parse("2024-01-01T11:00:00Z");

        Post post = Post.builder().caption("Post 1").postedAt(earlierTime).user(user).build();
        Post post2 = Post.builder().caption("Post 2").postedAt(laterTime).user(user).build(); // Add a one-second delay so that it seems posted after.

        userRepo.save(user);
        underTest.save(post);
        underTest.save(post2);

        // When
        Page<Post> results = underTest.getPostsByUsernameOrderByPostedAt(user.getUsername(), Pageable.unpaged());

        // Then
        assertEquals(2, results.getNumberOfElements());

        List<Post> resultsList = results.toList();

        assertEquals(user.getUsername(), resultsList.getFirst().getUser().getUsername());
        assertEquals(post2.getCaption(), resultsList.getFirst().getCaption()); // Check that the ordering worked by making sure the first one in the list is the last posted.

    }

    @Test
    void shouldNotGetPostsByUsernameOrderedByPostedAt(){

        // Given no posts by that user

        // When
        Page<Post> results = underTest.getPostsByUsernameOrderByPostedAt("testuser", Pageable.unpaged());

        // Then
        assertEquals(0, results.getNumberOfElements());

    }

    @Test
    void shouldGetPostMetadataByPostIdAndReturn1ofEach(){

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").build();
        ApplicationUser savedUser = userRepo.save(user);

        Post savedPost = underTest.save(Post.builder().caption("Post 1").user(savedUser).build());

        Like l1 = Like.builder().user(savedUser).post(savedPost).build();
        likeRepo.save(l1);

        Comment c1 =  Comment.builder().user(savedUser).content("Comment 1").post(savedPost).build();
        commentRepo.save(c1);

        // When
        Optional<PostMetadataDTO> data = underTest.getPostMetadataByPostId(savedPost.getId());

        // Then
        PostMetadataDTO meta = data.get();

        assertEquals(1, meta.likes());
        assertEquals(1, meta.comments());

    }

    @Test
    void shouldGetPostMetadataByPostIdAndReturn2likesFromDifferentUsers(){

        // Given
        ApplicationUser savedUser1 = userRepo.save(userRepo.save(ApplicationUser.builder().username("test1").build()));
        ApplicationUser savedUser2 = userRepo.save(userRepo.save(ApplicationUser.builder().username("test2").build()));

        Post savedPost = underTest.save(Post.builder().caption("Post 1").user(savedUser1).build());

        likeRepo.save(Like.builder().user(savedUser1).post(savedPost).build());
        likeRepo.save(Like.builder().user(savedUser2).post(savedPost).build());

        // When
        Optional<PostMetadataDTO> data = underTest.getPostMetadataByPostId(savedPost.getId());

        // Then
        PostMetadataDTO meta = data.get();

        assertEquals(2, meta.likes());
        assertEquals(0, meta.comments());

    }

    @Test
    void shouldGetPostMetadataByPostIdAndReturn0ofEach(){

        // Given
        ApplicationUser savedUser1 = userRepo.save(userRepo.save(ApplicationUser.builder().username("test1").build()));
        ApplicationUser savedUser2 = userRepo.save(userRepo.save(ApplicationUser.builder().username("test2").build()));

        Post postWithMeta = underTest.save(Post.builder().caption("Post with 2 likes and 0 comments").user(savedUser1).build());
        Post postWith0 = underTest.save(Post.builder().caption("Post with no likes or comments").user(savedUser1).build());

        likeRepo.save(Like.builder().user(savedUser1).post(postWithMeta).build());
        likeRepo.save(Like.builder().user(savedUser2).post(postWithMeta).build());

        // When
        Optional<PostMetadataDTO> data = underTest.getPostMetadataByPostId(postWith0.getId());

        // Then
        PostMetadataDTO meta = data.get();

        assertEquals(0, meta.likes());
        assertEquals(0, meta.comments());

    }

    @Test
    void shouldGetLikeStatusByPostIdAndReturnTrue(){

        // Given
        ApplicationUser savedUser = userRepo.save(userRepo.save(ApplicationUser.builder().username("test1").build()));

        Post likedPost = underTest.save(Post.builder().caption("Post liked by user").user(savedUser).build());

        likeRepo.save(Like.builder().user(savedUser).post(likedPost).build());

        // When
        PostLikeStatusDTO data = underTest.findLikeStatusByPostId(likedPost.getId(), savedUser.getId());

        // Then
        assertTrue(data.liked());
    }

    @Test
    void shouldGetLikeStatusByPostIdAndReturnFalseBecauseNobodyHasLiked(){

        // Given
        ApplicationUser savedUser = userRepo.save(ApplicationUser.builder().username("test1").build());

        Post post = underTest.save(Post.builder().caption("Post").user(savedUser).build());

        // When
        PostLikeStatusDTO data = underTest.findLikeStatusByPostId(post.getId(), savedUser.getId());

        // Then
        assertFalse(data.liked());

    }

    @Test
    void shouldGetLikeStatusByPostIdAndReturnFalseBecauseThatUserHasNotLiked(){

        // Given
        ApplicationUser userThatDidLike = userRepo.save(userRepo.save(ApplicationUser.builder().username("test1").build()));
        ApplicationUser userThatDidNotLike = userRepo.save(userRepo.save(ApplicationUser.builder().username("test2").build()));

        Post likedPost = underTest.save(Post.builder().caption("Post liked by one user").user(userThatDidLike).build());

        likeRepo.save(Like.builder().user(userThatDidLike).post(likedPost).build());

        // When
        PostLikeStatusDTO data = underTest.findLikeStatusByPostId(likedPost.getId(), userThatDidNotLike.getId());

        // Then
        assertFalse(data.liked());

    }

    @Test
    void shouldGetLikeStatusByPostIdsAndReturnTrueForAll(){

        // Given
        ApplicationUser userThatLiked = userRepo.save(ApplicationUser.builder().username("Test").build());

        Post likedPost1 = underTest.save(Post.builder().caption("Post liked by one user").user(userThatLiked).build());
        Post likedPost2 = underTest.save(Post.builder().caption("Another post").user(userThatLiked).build());

        likeRepo.save(Like.builder().user(userThatLiked).post(likedPost1).build());
        likeRepo.save(Like.builder().user(userThatLiked).post(likedPost2).build());

        List<UUID> postIds = List.of(likedPost1.getId(), likedPost2.getId());

        // When
        List<PostLikeStatusDTO> data =  underTest.findLikeStatusByPostIds(postIds, userThatLiked.getId());

        // Then
        assertTrue(data.getFirst().liked());
        assertTrue(data.getLast().liked());

    }

    @Test
    void shouldGetLikeStatusByPostIdsAndReturnTrueForOne(){

        // Given
        ApplicationUser userThatLiked = userRepo.save(ApplicationUser.builder().username("Test").build());

        Post likedPost = underTest.save(Post.builder().caption("Post liked by one user").user(userThatLiked).build());
        Post nonLikedPost = underTest.save(Post.builder().caption("Another post").user(userThatLiked).build());

        likeRepo.save(Like.builder().user(userThatLiked).post(likedPost).build());

        List<UUID> postIds = List.of(likedPost.getId(), nonLikedPost.getId());

        // When
        List<PostLikeStatusDTO> data =  underTest.findLikeStatusByPostIds(postIds, userThatLiked.getId());

        // Then
        PostLikeStatusDTO likedPostData = data.stream().filter(status -> status.postId().equals(likedPost.getId())).findFirst().orElse(null);
        PostLikeStatusDTO nonLikedPostData = data.stream().filter(status -> status.postId().equals(nonLikedPost.getId())).findFirst().orElse(null);

        assertTrue(likedPostData.liked());
        assertFalse(nonLikedPostData.liked());

    }

    @Test
    void shouldGetLikeStatusByPostIdsAndReturnDifferentResultFor2Users(){

        // Given
        ApplicationUser userThatLikedPost1 = userRepo.save(ApplicationUser.builder().username("User 1").build());
        ApplicationUser userThatLikedPost2 = userRepo.save(ApplicationUser.builder().username("User 2").build());

        ApplicationUser postCreator =  userRepo.save(ApplicationUser.builder().username("Post Creator").build());

        Post post1 = underTest.save(Post.builder().caption("Post liked by one user").user(postCreator).build());
        Post post2 = underTest.save(Post.builder().caption("Another post").user(postCreator).build());

        likeRepo.save(Like.builder().user(userThatLikedPost1).post(post1).build());
        likeRepo.save(Like.builder().user(userThatLikedPost2).post(post2).build());

        List<UUID> postIds = List.of(post1.getId(), post2.getId());

        // When
        List<PostLikeStatusDTO> dataForUser1 =  underTest.findLikeStatusByPostIds(postIds, userThatLikedPost1.getId());
        List<PostLikeStatusDTO> dataForUser2 =  underTest.findLikeStatusByPostIds(postIds, userThatLikedPost2.getId());

        // Then
        PostLikeStatusDTO post1FromUser1Perspective = dataForUser1.stream().filter(status -> status.postId().equals(post1.getId())).findFirst().orElse(null);
        PostLikeStatusDTO post2FromUser1Perspective = dataForUser1.stream().filter(status -> status.postId().equals(post2.getId())).findFirst().orElse(null);

        PostLikeStatusDTO post1FromUser2Perspective = dataForUser2.stream().filter(status -> status.postId().equals(post1.getId())).findFirst().orElse(null);
        PostLikeStatusDTO post2FromUser2Perspective = dataForUser2.stream().filter(status -> status.postId().equals(post2.getId())).findFirst().orElse(null);

        assertTrue(post1FromUser1Perspective.liked());
        assertFalse(post2FromUser1Perspective.liked());

        assertFalse(post1FromUser2Perspective.liked());
        assertTrue(post2FromUser2Perspective.liked());

    }

}