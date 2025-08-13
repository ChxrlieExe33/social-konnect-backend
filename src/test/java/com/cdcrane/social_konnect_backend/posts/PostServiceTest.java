package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.posts.dto.CreatePostDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostDTOWithLiked;
import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private PostService underTest;

    @Mock
    private PostRepository postRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private FileHandler fileHandler;


    @BeforeEach
    void setUp() {
        underTest = new PostService(postRepository, securityUtils, fileHandler);
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

    @Test
    void shouldGetPostById() {

        // Given
        Post p1 = Post.builder().caption("Post 1").build();

        given(postRepository.findById(p1.getId())).willReturn(Optional.of(p1));

        // When
        underTest.getPostById(p1.getId());

        // Then
        verify(postRepository).findById(p1.getId());

    }

    @Test
    void shouldNotGetPostById() {

        // Given no post
        given(postRepository.findById(any())).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostById(any())).isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(any());

    }

    @Test
    void shouldGetPostsByUsername() {

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").build();

        Post p1 = Post.builder()
                .id(UUID.randomUUID())
                .caption("Post 1")
                .user(user)
                .build();

        given(postRepository.getPostsByUsernameOrderByPostedAt(eq("testuser"), any()))
                .willReturn(new PageImpl<>(List.of(p1)));

        // When
        Page<Post> results = underTest.getPostsByUsername("testuser", Pageable.unpaged());

        // Then
        verify(postRepository).getPostsByUsernameOrderByPostedAt("testuser", Pageable.unpaged());

        assertThat(results.getNumberOfElements()).isEqualTo(1);
        List<Post> resultsList = results.toList();

        assertThat(resultsList.getFirst().getCaption()).isEqualTo(p1.getCaption());
        assertThat(resultsList.getFirst().getUser().getUsername()).isEqualTo(user.getUsername());

    }

    @Test
    void shouldNotGetPostsByUsername(){

        // Given no posts
        given(postRepository.getPostsByUsernameOrderByPostedAt(eq("testuser"), any()))
                .willReturn(Page.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostsByUsername("testuser", Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);

    }

    @Test
    void shouldGetPostsWithLiked() {

        // Given
        ApplicationUser user =  ApplicationUser.builder().username("testuser").profilePictureUrl("http://google.com").id(1L).build();
        given(securityUtils.getCurrentAuth()).willReturn(user);

        Post p1 =  Post.builder().caption("Post 1").id(UUID.randomUUID()).user(user).build();
        Post p2 =  Post.builder().caption("Post 2").id(UUID.randomUUID()).user(user).build();
        Page<Post> posts = new PageImpl<>(List.of(p1, p2));
        given(postRepository.getPostsOrderByPostedAt(any())).willReturn(posts);

        PostLikeStatusDTO statusP1 = new PostLikeStatusDTO(p1.getId(), true);
        PostLikeStatusDTO statusP2 = new PostLikeStatusDTO(p2.getId(), true);
        given(postRepository.findLikeStatusByPostIds(any(), any())).willReturn(List.of(statusP1, statusP2));

        // When
        Page<PostDTOWithLiked> response = underTest.getPostsWithLiked(Pageable.unpaged());

        // Then
        verify(postRepository).getPostsOrderByPostedAt(any());
        verify(postRepository).findLikeStatusByPostIds(List.of(p1.getId(), p2.getId()), user.getId());

        List<PostDTOWithLiked> data = response.stream().toList();

        assertThat(data.size()).isEqualTo(2);
        assertThat(data.getFirst().liked()).isTrue();
        assertThat(data.getLast().liked()).isTrue();

    }

    @Test
    void shouldNotGetPostsWithLikedBecauseNoPosts() {

        // Given no posts
        given(postRepository.getPostsOrderByPostedAt(any())).willReturn(Page.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostsWithLiked(Pageable.unpaged())).isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, never()).findLikeStatusByPostIds(any(), any());


    }

    @Test
    void shouldGetPostsWithLikedByUsername() {

        // Given
        ApplicationUser user =  ApplicationUser.builder().username("testuser").profilePictureUrl("http://google.com").id(1L).build();

        Post p1 =  Post.builder().caption("Post 1").id(UUID.randomUUID()).user(user).build();
        Post p2 =  Post.builder().caption("Post 2").id(UUID.randomUUID()).user(user).build();
        Page<Post> posts = new PageImpl<>(List.of(p1, p2));
        given(postRepository.getPostsByUsernameOrderByPostedAt(any(), any())).willReturn(posts);

        given(securityUtils.getCurrentAuth()).willReturn(user);

        PostLikeStatusDTO statusP1 = new PostLikeStatusDTO(p1.getId(), true);
        PostLikeStatusDTO statusP2 = new PostLikeStatusDTO(p2.getId(), true);
        given(postRepository.findLikeStatusByPostIds(any(), any())).willReturn(List.of(statusP1, statusP2));

        // When
        Page<PostDTOWithLiked> response = underTest.getPostsWithLikedByUsername(any(), eq(Pageable.unpaged()));

        // Then
        verify(postRepository).getPostsByUsernameOrderByPostedAt(any(), any());
        verify(postRepository).findLikeStatusByPostIds(List.of(p1.getId(), p2.getId()), user.getId());

        List<PostDTOWithLiked> data = response.stream().toList();

        assertThat(data.size()).isEqualTo(2);
        assertThat(data.getFirst().liked()).isTrue();
        assertThat(data.getLast().liked()).isTrue();

    }

    @Test
    void shouldNotGetPostsWithLikedByUsernameBecauseNoPosts() {

        // Given no posts
        given(postRepository.getPostsByUsernameOrderByPostedAt(any(), any())).willReturn(Page.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostsWithLikedByUsername(any(), any())).isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, never()).findLikeStatusByPostIds(any(), any());

    }

    @Test
    void shouldGetPostWithLikedById() {

        // Given
        ApplicationUser user = ApplicationUser.builder().username("testuser").profilePictureUrl("http://google.com").id(1L).build();
        given(securityUtils.getCurrentAuth()).willReturn(user);

        Post p1 = Post.builder().caption("Post 1").id(UUID.randomUUID()).user(user).build();
        given(postRepository.findById(p1.getId())).willReturn(Optional.of(p1));

        PostLikeStatusDTO statusP1 = new PostLikeStatusDTO(p1.getId(), true);
        given(postRepository.findLikeStatusByPostId(p1.getId(), user.getId())).willReturn(statusP1);

        // When
        PostDTOWithLiked response = underTest.getPostWithLikedById(p1.getId());

        // Then
        verify(postRepository).findById(p1.getId());
        verify(postRepository).findLikeStatusByPostId(p1.getId(), user.getId());

        assertThat(response.liked()).isTrue();

    }

    @Test
    void shouldNotGetPostWithLikedByIdBecauseNoPost() {

        // Given no posts
        UUID invalidPost =  UUID.randomUUID();

        given(postRepository.findById(invalidPost)).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostWithLikedById(invalidPost)).isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, never()).findLikeStatusByPostId(any(), any());

    }

    @Test
    void shouldGetPostMetadataByPostId(){

        // Given
        PostMetadataDTO metadata = new PostMetadataDTO(UUID.randomUUID(), 1L, 2L);

        given(postRepository.getPostMetadataByPostId(metadata.postId())).willReturn(Optional.of(metadata));

        // When
        underTest.getPostMetadataByPostId(metadata.postId());

        // Then
        verify(postRepository).getPostMetadataByPostId(metadata.postId());

    }

    @Test
    void shouldNotGetPostMetadataByPostIdBecauseNoPost() {

        // Given no post
        given(postRepository.getPostMetadataByPostId(any())).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getPostMetadataByPostId(UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);

    }

    @Test
    void shouldSavePostWithMedia(){

        // Given
        // Create mock multipart file
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);

        CreatePostDTO dto = new CreatePostDTO("Test caption", List.of(mockFile));

        PostMedia media = new PostMedia(UUID.randomUUID(), "http://test.com", "IMAGE", "generated_name.png");

        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();

        given(fileHandler.saveFiles(any())).willReturn(List.of(media));
        given(securityUtils.getCurrentAuth()).willReturn(user);

        // When
        underTest.savePost(dto);

        // Then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        verify(fileHandler).saveFiles(dto.files());

        verify(securityUtils).getCurrentAuth();

        verify(postRepository).save(captor.capture());

        Post result = captor.getValue();

        assertThat(result.getCaption()).isEqualTo(dto.caption());
        assertThat(result.getUser().getUsername()).isEqualTo(user.getUsername());

        assertThat(result.getPostMedia())
                .as("Should have 1 post media")
                .hasSize(1);

        assertThat(result.getPostMedia().getFirst().getMediaType()).isEqualTo(media.getMediaType());

    }

    @Test
    void shouldSavePostWithoutMedia(){

        // Given
        CreatePostDTO dto = new CreatePostDTO("Test caption", null);

        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();

        given(securityUtils.getCurrentAuth()).willReturn(user);

        // When
        underTest.savePost(dto);

        // Then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        verify(fileHandler, never()).saveFiles(any()); // Make sure its never called

        verify(securityUtils).getCurrentAuth(); // Make sure it is called

        verify(postRepository).save(captor.capture()); // Make sure its called

        Post result = captor.getValue();

        assertThat(result.getCaption()).isEqualTo(dto.caption());

        assertThat(result.getUser().getUsername()).isEqualTo(user.getUsername());

        assertThat(result.getPostMedia()).isNullOrEmpty();

    }

    @Test
    void shouldDeletePostWithMedia(){

        // Given
        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();

        PostMedia media = PostMedia.builder()
                .id(UUID.randomUUID()).mediaUrl("http://test.com")
                .fileName("testfile.png").mediaType("IMAGE").build();

        Post p1 = Post.builder()
                .id(UUID.randomUUID()).caption("Post 1").user(user).postMedia(List.of(media)).build();

        given(securityUtils.getCurrentAuth()).willReturn(user);
        given(postRepository.findById(any())).willReturn(Optional.of(p1));

        // When
        underTest.deletePost(p1.getId());

        // Then
        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        verify(securityUtils).getCurrentAuth();

        verify(postRepository).findById(p1.getId());

        verify(postRepository).deleteById(captor.capture());

        verify(fileHandler).deleteFile(media.getFileName());

        UUID result = captor.getValue();

        assertThat(result).isEqualTo(p1.getId());

    }

    @Test
    void shouldDeletePostWithoutMedia(){

        // Given
        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();
        Post p1 = Post.builder()
                .id(UUID.randomUUID()).caption("Post 1").user(user).postMedia(null).build();

        given(securityUtils.getCurrentAuth()).willReturn(user);
        given(postRepository.findById(any())).willReturn(Optional.of(p1));

        // When
        underTest.deletePost(p1.getId());

        // Then
        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);

        verify(securityUtils).getCurrentAuth(); // Make sure it retrieves the authed user

        verify(postRepository).findById(p1.getId()); // Make sure it finds the post

        verify(postRepository).deleteById(captor.capture()); // Make sure it was deleted

        verify(fileHandler, never()).deleteFile(any()); // Make sure this was never called

        UUID result = captor.getValue();

        assertThat(result).isEqualTo(p1.getId());

    }

    @Test
    void shouldNotDeletePostBecausePostNotFound(){

        // Given
        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();
        given(securityUtils.getCurrentAuth()).willReturn(user);

        given(postRepository.findById(any())).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.deletePost(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(any());

        verify(postRepository, never()).deleteById(any());
        verify(fileHandler, never()).deleteFile(any());

    }

    @Test
    void shouldNotDeletePostBecauseUserNotOwner(){

        // Given
        ApplicationUser user1 = ApplicationUser.builder().id(1L).username("testuser").build();
        Post p1 = Post.builder() // Post owned by user1
                .id(UUID.randomUUID()).caption("Post 1").user(user1).postMedia(null).build();

        ApplicationUser user2 = ApplicationUser.builder().id(2L).username("testuser").build();

        // User2 is trying to delete
        given(securityUtils.getCurrentAuth()).willReturn(user2);
        given(postRepository.findById(any())).willReturn(Optional.of(p1));

        // Then
        assertThatThrownBy(() -> underTest.deletePost(UUID.randomUUID()))
                .isInstanceOf(ActionNotPermittedException.class);

        verify(postRepository).findById(any());

        verify(postRepository, never()).deleteById(any());
        verify(fileHandler, never()).deleteFile(any());

    }

    @Test
    void shouldUpdatePostCaption(){

        // Given
        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();
        Post p1 = Post.builder()
                .id(UUID.randomUUID()).caption("Post 1").user(user).postMedia(null).build();

        given(securityUtils.getCurrentAuth()).willReturn(user);
        given(postRepository.findById(any())).willReturn(Optional.of(p1));

        String newCaption = "Post 1 updated";

        // When
        underTest.updatePostCaption(p1.getId(), newCaption);

        // Then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        verify(securityUtils).getCurrentAuth();
        verify(postRepository).findById(p1.getId());
        verify(postRepository).save(captor.capture());

        Post result = captor.getValue();

        assertThat(result.getCaption()).isEqualTo(newCaption);

    }

    @Test
    void shouldNotUpdatePostCaptionBecausePostNotFound(){

        // Given
        ApplicationUser user = ApplicationUser.builder().id(1L).username("testuser").build();

        given(securityUtils.getCurrentAuth()).willReturn(user);
        given(postRepository.findById(any())).willReturn(Optional.empty());

        UUID id = UUID.randomUUID();

        // Then
        assertThatThrownBy(() -> underTest.updatePostCaption(id, "New caption!"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(id);
        verify(postRepository, never()).save(any());

    }

    @Test
    void shouldNotUpdatePostCaptionBecauseUserNotOwner(){

        // Given
        ApplicationUser user1 = ApplicationUser.builder().id(1L).username("testuser").build();
        Post p1 = Post.builder() // Post owned by user1
                .id(UUID.randomUUID()).caption("Post 1").user(user1).postMedia(null).build();

        ApplicationUser user2 = ApplicationUser.builder().id(2L).username("testuser").build();

        // User2 is trying to update
        given(securityUtils.getCurrentAuth()).willReturn(user2);
        given(postRepository.findById(any())).willReturn(Optional.of(p1));

        // Then
        assertThatThrownBy(() -> underTest.updatePostCaption(p1.getId(), "New caption!"))
                .isInstanceOf(ActionNotPermittedException.class);

        verify(postRepository).findById(p1.getId());
        verify(postRepository, never()).save(any());

    }

}