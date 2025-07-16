package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private PostService underTest;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private FileHandler fileHandler;

    @BeforeEach
    void setUp() {
        underTest = new PostService(postRepository, userRepository, securityUtils, fileHandler);
    }

    @Test
    void shouldGetAllPosts() {

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").build();

        Post p1 = Post.builder()
                .id(UUID.randomUUID())
                .caption("Post 1")
                .user(user)
                .build();

        Post p2 = Post.builder()
                .id(UUID.randomUUID())
                .caption("Post 2")
                .user(user)
                .build();

        Page<Post> posts = new PageImpl<>(List.of(p1, p2));

        given(postRepository.getPostsOrderByPostedAt(any())).willReturn(posts);

        // When
        Page<Post> results = underTest.getAllPosts(Pageable.unpaged());

        // Then
        assertThat(results.getNumberOfElements()).isEqualTo(2);

        List<Post> resultsList = results.toList();

        assertThat(resultsList.getFirst().getCaption()).isEqualTo(p1.getCaption());
        assertThat(resultsList.getFirst().getUser().getUsername()).isEqualTo(user.getUsername());


    }

    @Test
    void shouldNotGetAllPosts() {

        // Given no posts
        given(postRepository.getPostsOrderByPostedAt(any())).willReturn(Page.empty());

        assertThatThrownBy(() -> underTest.getAllPosts(Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);


    }
}