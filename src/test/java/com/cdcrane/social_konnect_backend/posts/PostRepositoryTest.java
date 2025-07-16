package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository underTest;

    @Autowired
    private UserRepository userRepo;

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
    void shouldGetPostsByUsernameOrderedByPostedAt(){

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").build();

        Post post = Post.builder().caption("Post 1").user(user).build();
        Post post2 = Post.builder().caption("Post 2").user(user).build();

        userRepo.save(user);
        underTest.save(post);
        underTest.save(post2);

        // When
        Page<Post> results = underTest.getPostsByUsernameOrderByPostedAt(user.getUsername(), Pageable.unpaged());

        // Then
        assertEquals(2, results.getNumberOfElements());

        List<Post> resultsList = results.toList();

        assertEquals(user.getUsername(), resultsList.getFirst().getUser().getUsername());
        assertEquals(post2.getCaption(), resultsList.getFirst().getCaption()); // Check the first post has the caption of the last created post.

    }

    @Test
    void shouldNotGetPostsByUsernameOrderedByPostedAt(){

        // Given no posts by that user

        // When
        Page<Post> results = underTest.getPostsByUsernameOrderByPostedAt("testuser", Pageable.unpaged());

        // Then
        assertEquals(0, results.getNumberOfElements());

    }
}